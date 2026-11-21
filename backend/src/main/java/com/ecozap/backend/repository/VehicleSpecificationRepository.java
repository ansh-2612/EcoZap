package com.ecozap.backend.repository;

import com.ecozap.backend.model.VehicleSpecification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleSpecificationRepository
        extends MongoRepository<VehicleSpecification, String> {

    List<VehicleSpecification> findTop20ByOrderByBrandAscModelAsc();

    List<VehicleSpecification> findByBrandIgnoreCaseContainingOrModelIgnoreCaseContaining(
            String brand,
            String model
    );
}