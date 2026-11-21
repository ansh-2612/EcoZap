package com.ecozap.backend.service;

import com.ecozap.backend.dto.*;
import com.ecozap.backend.model.*;
import com.ecozap.backend.repository.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final Set<BookingStatus> ACTIVE = EnumSet.of(BookingStatus.PENDING,
            BookingStatus.CONFIRMED, BookingStatus.ARRIVED, BookingStatus.SERVICING);
    private final BookingRepository bookings;
    private final VehicleRepository vehicles;
    private final VehicleSpecificationRepository specifications;
    private final StationRepository stations;
    private final StationSlotRepository slots;
    private final RedisReservationService locks;
    private final MongoTemplate mongo;

    public BookingService(BookingRepository bookings, VehicleRepository vehicles,
                          VehicleSpecificationRepository specifications, StationRepository stations,
                          StationSlotRepository slots, RedisReservationService locks, MongoTemplate mongo) {
        this.bookings = bookings;
        this.vehicles = vehicles;
        this.specifications = specifications;
        this.stations = stations;
        this.slots = slots;
        this.locks = locks;
        this.mongo = mongo;
    }

    @PostConstruct
    void indexReservations() {
        // Older bookings have no reservationKey. They still participate in the overlap query.
        mongo.getCollection(mongo.getCollectionName(Booking.class)).createIndex(
                Indexes.ascending("reservationKey"), new IndexOptions().unique(true).sparse(true));
    }

    public List<SlotAvailability> availability(String stationId, LocalDate date) {
        if (!stations.existsById(stationId)) throw missing("Station not found");
        Instant now = Instant.now();
        LocalDateTime localNow = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        List<Booking> active = mongo.find(query(where("stationId").is(stationId).and("status").in(ACTIVE)
                .orOperator(where("expectedEndTime").gt(localNow), where("expectedEndTime").is(null))), Booking.class);
        return slots.findByStationId(stationId).stream().map(slot -> {
            BookingWindow window = BookingWindow.forSlot(slot.getTime(), date, now);
            boolean available = window.isBookable(now) && active.stream()
                    .noneMatch(b -> slot.getId().equals(b.getSlotId()) && overlaps(b, window));
            return new SlotAvailability(slot.getId(), slot.getSlotNumber(), slot.getTime(), slot.getVehicleType(),
                    slot.getChargerPower(), slot.getEstimatedServiceTimeMinutes(), window.scheduled(), available);
        }).toList();
    }

    public BookingResponse createBooking(BookingRequest request, String userId) {
        UUID requestId = request.getRequestId();
        String id = requestId == null ? UUID.randomUUID().toString() :
                UUID.nameUUIDFromBytes((userId + "/" + requestId).getBytes(StandardCharsets.UTF_8)).toString();
        if (requestId != null) {
            Booking existing = bookings.findById(id).orElse(null);
            if (existing != null) return replay(existing, userId, request);
        }
        Vehicle vehicle = vehicles.findById(request.getVehicleId())
                .filter(v -> userId.equals(v.getUserId())).orElseThrow(() -> missing("Vehicle not found"));
        VehicleSpecification spec = specifications.findById(vehicle.getVehicleSpecificationId())
                .orElseThrow(() -> missing("Vehicle specification not found"));
        StationSlot slot = slots.findById(request.getSlotId())
                .filter(s -> request.getStationId().equals(s.getStationId())).orElseThrow(() -> missing("Slot not found at this station"));
        if (!stations.existsById(slot.getStationId())) throw missing("Station not found");
        if (slot.getVehicleType() != spec.getType()) throw bad("Choose a vehicle compatible with this slot");
        int minutes = serviceMinutes(request, spec, slot);
        String owner = UUID.randomUUID().toString();
        if (!locks.acquire(slot.getId(), owner)) throw conflict("Another booking is being processed. Refresh the slots and try again.");
        try {
            Instant now = Instant.now();
            LocalDateTime bookingTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
            BookingWindow window = BookingWindow.forSlot(slot.getTime(), request.getServiceDate(), now);
            if (!window.isBookable(now)) throw conflict("This window is no longer available for the selected day");
            LocalDateTime end = window.start().plusMinutes(minutes);
            if (window.end() != null && end.isAfter(window.end())) throw bad("The service would take longer than this window");
            if (requestId != null) {
                Booking existing = bookings.findById(id).orElse(null);
                if (existing != null) return replay(existing, userId, request);
            }
            expire(where("slotId").is(slot.getId()), bookingTime);
            List<Booking> active = mongo.find(query(where("slotId").is(slot.getId()).and("status").in(ACTIVE)), Booking.class);
            if (active.stream().anyMatch(b -> overlaps(b, window))) throw conflict("This slot is already booked");
            Booking booking = new Booking();
            booking.setId(id);
            booking.setUserId(userId);
            booking.setVehicleId(vehicle.getId());
            booking.setSlotId(slot.getId());
            booking.setStationId(slot.getStationId());
            booking.setReservationKey(window.reservationKey(slot.getId()));
            booking.setRequestedDate(request.getServiceDate());
            booking.setRequestedCurrentEnergy(request.getCurrentEnergy());
            booking.setRequestedTargetEnergy(request.getTargetEnergy());
            booking.setBookingTime(bookingTime);
            booking.setStartTime(window.start());
            booking.setExpectedEndTime(end);
            booking.setStatus(BookingStatus.CONFIRMED);
            try {
                return response(bookings.insert(booking));
            } catch (DuplicateKeyException e) {
                Booking duplicate = requestId == null ? null : bookings.findById(id).orElse(null);
                if (duplicate != null) return replay(duplicate, userId, request);
                throw conflict("This slot is already booked");
            }
        } finally {
            // A failed unlock expires on its own. A successful Mongo insert is still a confirmation.
            try { locks.release(slot.getId(), owner); }
            catch (DataAccessException e) { log.warn("Could not release booking lock; it will expire"); }
        }
    }

    public List<BookingResponse> history(String userId) {
        expire(where("userId").is(userId), LocalDateTime.now());
        return bookings.findByUserIdOrderByBookingTimeDesc(userId).stream().map(this::response).toList();
    }

    public BookingResponse cancelBooking(String bookingId, String userId) {
        LocalDateTime now = LocalDateTime.now();
        expire(where("_id").is(bookingId).and("userId").is(userId), now);
        Query owned = query(where("_id").is(bookingId).and("userId").is(userId)
                .and("status").is(BookingStatus.CONFIRMED));
        Booking cancelled = mongo.findAndModify(owned, new Update().set("status", BookingStatus.CANCELLED)
                .unset("reservationKey"), FindAndModifyOptions.options().returnNew(true), Booking.class);
        if (cancelled != null) return response(cancelled);
        Booking current = bookings.findById(bookingId).filter(b -> userId.equals(b.getUserId()))
                .orElseThrow(() -> missing("Booking not found"));
        if (current.getStatus() == BookingStatus.CANCELLED) return response(current);
        throw conflict("Only a current confirmed booking can be cancelled");
    }

    private void expire(Criteria scope, LocalDateTime now) {
        Query ended = query(scope.and("status").in(ACTIVE).and("expectedEndTime").lte(now));
        mongo.updateMulti(ended, new Update().set("status", BookingStatus.EXPIRED).unset("reservationKey"), Booking.class);
    }

    private boolean overlaps(Booking b, BookingWindow window) {
        if (b.getExpectedEndTime() != null && !b.getExpectedEndTime().isAfter(window.start())) return false;
        return window.end() == null || b.getStartTime() == null || b.getStartTime().isBefore(window.end());
    }

    private int serviceMinutes(BookingRequest req, VehicleSpecification spec, StationSlot slot) {
        if (spec.getType() == VehicleType.CNG) {
            Integer minutes = slot.getEstimatedServiceTimeMinutes();
            if (minutes == null || minutes < 1) throw bad("This CNG slot has no service duration");
            return minutes;
        }
        Double current = req.getCurrentEnergy(), target = req.getTargetEnergy();
        if (current == null || target == null || !Double.isFinite(current) || !Double.isFinite(target)
                || current < 0 || target <= current) throw bad("Enter a target energy above the current energy");
        if (spec.getBatteryCapacity() == null || target > spec.getBatteryCapacity()) throw bad("Check the vehicle battery capacity");
        if (slot.getChargerPower() == null || slot.getChargerPower() <= 0) throw bad("This slot has no charger power configured");
        return Math.max(1, (int) Math.ceil(60 * (target - current) / slot.getChargerPower()));
    }

    private BookingResponse replay(Booking b, String userId, BookingRequest r) {
        if (!userId.equals(b.getUserId()) || !b.getVehicleId().equals(r.getVehicleId())
                || !b.getStationId().equals(r.getStationId()) || !b.getSlotId().equals(r.getSlotId())
                || !Objects.equals(b.getRequestedDate(), r.getServiceDate())
                || !Objects.equals(b.getRequestedCurrentEnergy(), r.getCurrentEnergy())
                || !Objects.equals(b.getRequestedTargetEnergy(), r.getTargetEnergy()))
            throw conflict("This request was already used for a different booking");
        return response(b);
    }

    private BookingResponse response(Booking b) {
        return new BookingResponse(b.getId(), b.getVehicleId(), b.getStationId(), b.getSlotId(),
                BookingWindow.stationTime(b.getBookingTime()), BookingWindow.stationTime(b.getStartTime()),
                BookingWindow.stationTime(b.getExpectedEndTime()), b.getStatus());
    }

    private static ResponseStatusException missing(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    private static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private static ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
}
