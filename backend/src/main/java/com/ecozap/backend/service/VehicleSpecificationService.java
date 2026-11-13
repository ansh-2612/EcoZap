package com.ecozap.backend.service;

import com.ecozap.backend.dto.VehicleSuggestionResponse;
import com.ecozap.backend.model.VehicleSpecification;
import com.ecozap.backend.repository.VehicleSpecificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleSpecificationService {

    private final VehicleSpecificationRepository repository;

    public VehicleSpecificationService(VehicleSpecificationRepository repository) {
        this.repository = repository;
    }

    public List<VehicleSuggestionResponse> searchVehicles(String query) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<VehicleSpecification> vehicles =
                repository.findByBrandIgnoreCaseContainingOrModelIgnoreCaseContaining(
                        query.trim(),
                        query.trim()
                );

        return vehicles.stream()
                .map(vehicle -> new VehicleSuggestionResponse(
                        vehicle.getId(),
                        vehicle.getBrand(),
                        vehicle.getModel(),
                        vehicle.getType()
                ))
                .collect(Collectors.toList());
    }
}