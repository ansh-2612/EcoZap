package com.ecozap.backend.Controller;

import com.ecozap.backend.model.Station;
import com.ecozap.backend.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(
                stationService.getAllStations()
        );
    }

    @PostMapping
    public ResponseEntity<Station> addStation(
            @RequestBody Station station) {

        return ResponseEntity.ok(
                stationService.addStation(station)
        );
    }
}