package com.ecozap.backend.repository;

import com.ecozap.backend.model.StationSlot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StationSlotRepository
        extends MongoRepository<StationSlot, String> {

    List<StationSlot> findByStationId(String stationId);
}