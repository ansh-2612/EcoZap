package com.ecozap.backend.service;

import com.ecozap.backend.dto.BookingRequest;
import com.ecozap.backend.dto.BookingResponse;
import com.ecozap.backend.model.*;
import com.ecozap.backend.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleSpecificationRepository vehicleSpecificationRepository;
    private final StationRepository stationRepository;
    private final StationSlotRepository stationSlotRepository;
    private final RedisReservationService redisReservationService;

    public BookingService(
            BookingRepository bookingRepository,
            VehicleRepository vehicleRepository,
            VehicleSpecificationRepository vehicleSpecificationRepository,
            StationRepository stationRepository,
            StationSlotRepository stationSlotRepository,
            RedisReservationService redisReservationService) {

        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleSpecificationRepository =
                vehicleSpecificationRepository;
        this.stationRepository = stationRepository;
        this.stationSlotRepository = stationSlotRepository;
        this.redisReservationService =
                redisReservationService;
    }

    @Transactional
    public BookingResponse createBooking(
            BookingRequest request,
            String userId) {

        Vehicle vehicle = vehicleRepository
                .findById(request.getVehicleId())
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        if (!vehicle.getUserId().equals(userId)) {
            throw new RuntimeException(
                    "Vehicle does not belong to this user");
        }

        VehicleSpecification specification =
                vehicleSpecificationRepository
                        .findById(
                                vehicle.getVehicleSpecificationId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle specification not found"));

        Station station = stationRepository
                .findById(request.getStationId())
                .orElseThrow(() ->
                        new RuntimeException("Station not found"));

        StationSlot slot = stationSlotRepository
                .findById(request.getSlotId())
                .orElseThrow(() ->
                        new RuntimeException("Slot not found"));

        if (!slot.getStationId().equals(station.getId())) {
            throw new RuntimeException(
                    "Slot does not belong to this station");
        }

        if (specification.getType() != slot.getVehicleType()) {
            throw new RuntimeException(
                    "This slot is not compatible with your vehicle");
        }

        List<Booking> bookings =
                bookingRepository.findBySlotId(slot.getId());

        boolean slotBusy = bookings.stream()
                .anyMatch(this::isActiveBooking);

        if (slotBusy) {
            throw new RuntimeException(
                    "This slot is already booked");
        }

        int serviceMinutes =
                calculateServiceTime(
                        request,
                        specification,
                        slot
                );

        LocalDateTime bookingTime =
                LocalDateTime.now();

        LocalDateTime startTime =
                bookingTime;

        LocalDateTime expectedEndTime =
                startTime.plusMinutes(serviceMinutes);

        String bookingId =
                UUID.randomUUID().toString();

        boolean reserved =
                redisReservationService.reserveSlot(
                        slot.getId(),
                        bookingId,
                        serviceMinutes
                );

        if (!reserved) {
            throw new RuntimeException(
                    "This slot is currently reserved");
        }

        Booking booking = new Booking();

        booking.setId(bookingId);
        booking.setUserId(userId);
        booking.setVehicleId(vehicle.getId());
        booking.setStationId(station.getId());
        booking.setSlotId(slot.getId());

        booking.setBookingTime(bookingTime);
        booking.setStartTime(startTime);
        booking.setExpectedEndTime(expectedEndTime);

        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        try {

            Booking savedBooking =
                    bookingRepository.save(booking);

            return toResponse(savedBooking);

        } catch (Exception e) {

            redisReservationService.releaseSlot(
                    slot.getId()
            );

            throw e;
        }
    }

    // Cancel booking
    public BookingResponse cancelBooking(
            String bookingId,
            String userId) {

        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException(
                    "You are not allowed to cancel this booking");
        }

        if (booking.getStatus()
                != BookingStatus.CONFIRMED) {

            throw new RuntimeException(
                    "Only confirmed bookings can be cancelled");
        }

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        Booking savedBooking =
                bookingRepository.save(booking);

        // Release Redis reservation
        redisReservationService.releaseSlot(
                booking.getSlotId()
        );

        return toResponse(savedBooking);
    }

    private boolean isActiveBooking(
            Booking booking) {

        return booking.getStatus()
                == BookingStatus.PENDING

                || booking.getStatus()
                == BookingStatus.CONFIRMED

                || booking.getStatus()
                == BookingStatus.ARRIVED

                || booking.getStatus()
                == BookingStatus.SERVICING;
    }

    private int calculateServiceTime(
            BookingRequest request,
            VehicleSpecification specification,
            StationSlot slot) {

        if (specification.getType()
                == VehicleType.EV) {

            if (request.getCurrentEnergy() == null
                    || request.getTargetEnergy() == null) {

                throw new RuntimeException(
                        "Current and target energy are required for EV");
            }

            if (slot.getChargerPower() == null
                    || slot.getChargerPower() <= 0) {

                throw new RuntimeException(
                        "Charger power is not configured for this slot");
            }

            if (request.getTargetEnergy()
                    <= request.getCurrentEnergy()) {

                throw new RuntimeException(
                        "Target energy must be greater than current energy");
            }

            if (request.getTargetEnergy()
                    > specification.getBatteryCapacity()) {

                throw new RuntimeException(
                        "Target energy exceeds battery capacity");
            }

            double energyRequired =
                    request.getTargetEnergy()
                            - request.getCurrentEnergy();

            double hours =
                    energyRequired
                            / slot.getChargerPower();

            return Math.max(
                    1,
                    (int) Math.ceil(hours * 60)
            );
        }

        if (slot.getEstimatedServiceTimeMinutes()
                == null
                || slot.getEstimatedServiceTimeMinutes() <= 0) {

            throw new RuntimeException(
                    "Service time is not configured for this CNG slot");
        }

        return slot.getEstimatedServiceTimeMinutes();
    }

    private BookingResponse toResponse(
            Booking booking) {

        return new BookingResponse(
                booking.getId(),
                booking.getVehicleId(),
                booking.getStationId(),
                booking.getSlotId(),
                booking.getBookingTime(),
                booking.getStartTime(),
                booking.getExpectedEndTime(),
                booking.getStatus()
        );
    }
}