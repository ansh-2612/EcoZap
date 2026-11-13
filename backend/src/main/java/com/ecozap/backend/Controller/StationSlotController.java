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

    public StationSlotController(StationSlotService stationSlotService) {
        this.stationSlotService = stationSlotService;
    }

    @GetMapping("/{stationId}/slots")
    public ResponseEntity<List<StationSlot>> getSlots(
            @PathVariable String stationId) {

        return ResponseEntity.ok(
                stationSlotService.getSlotsByStation(stationId)
        );
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