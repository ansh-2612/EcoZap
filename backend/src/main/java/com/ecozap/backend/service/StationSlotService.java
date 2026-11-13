package com.ecozap.backend.service;

import com.ecozap.backend.model.StationSlot;
import com.ecozap.backend.repository.StationSlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationSlotService {

    private final StationSlotRepository stationSlotRepository;

    public StationSlotService(StationSlotRepository stationSlotRepository) {
        this.stationSlotRepository = stationSlotRepository;
    }

    public List<StationSlot> getSlotsByStation(String stationId) {
        return stationSlotRepository.findByStationId(stationId);
    }

    public StationSlot addSlot(StationSlot slot) {
        return stationSlotRepository.save(slot);
    }
}