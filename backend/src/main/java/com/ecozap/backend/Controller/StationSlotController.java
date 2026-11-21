package com.ecozap.backend.Controller;

import com.ecozap.backend.model.StationSlot;
import com.ecozap.backend.service.StationSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationSlotController {

    private final StationSlotService stationSlotService;

    private final com.ecozap.backend.service.BookingService bookings;

    public StationSlotController(StationSlotService stationSlotService,
                                com.ecozap.backend.service.BookingService bookings) {
        this.bookings = bookings;
        this.stationSlotService = stationSlotService;
    }

    @GetMapping("/{stationId}/slots")
    public ResponseEntity<List<StationSlot>> getSlots(
            @PathVariable String stationId) {

        return ResponseEntity.ok(
                stationSlotService.getSlotsByStation(stationId)
        );
    }

    @GetMapping("/{stationId}/availability")
    public List<com.ecozap.backend.dto.SlotAvailability> availability(
            @PathVariable String stationId, @RequestParam java.time.LocalDate date) {
        return bookings.availability(stationId, date);
    }

    @PostMapping("/{stationId}/slots")
    public ResponseEntity<StationSlot> addSlot(
            @PathVariable String stationId,
            @RequestBody StationSlot slot) {

        slot.setStationId(stationId);

        return ResponseEntity.ok(
                stationSlotService.addSlot(slot)
        );
    }
}