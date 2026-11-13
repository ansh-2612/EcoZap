package com.ecozap.backend.repository;

import com.ecozap.backend.model.Station;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StationRepository extends MongoRepository<Station, String> {
}