package com.ecozap.backend.service;

import com.ecozap.backend.model.Station;
import com.ecozap.backend.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Station addStation(Station station) {
        return stationRepository.save(station);
    }
}