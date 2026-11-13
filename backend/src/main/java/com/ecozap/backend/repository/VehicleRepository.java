package com.ecozap.backend.repository;

import com.ecozap.backend.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    List<Vehicle> findByUserId(String userId);
}