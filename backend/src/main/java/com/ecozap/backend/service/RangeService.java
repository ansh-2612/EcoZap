package com.ecozap.backend.service;

import com.ecozap.backend.dto.RangeCheckRequest;
import com.ecozap.backend.dto.RangeCheckResponse;
import com.ecozap.backend.model.Vehicle;
import com.ecozap.backend.model.VehicleSpecification;
import com.ecozap.backend.repository.VehicleRepository;
import com.ecozap.backend.repository.VehicleSpecificationRepository;
import org.springframework.stereotype.Service;

@Service
public class RangeService {

    private final VehicleRepository vehicleRepository;
    private final VehicleSpecificationRepository specificationRepository;

    public RangeService(
            VehicleRepository vehicleRepository,
            VehicleSpecificationRepository specificationRepository) {

        this.vehicleRepository = vehicleRepository;
        this.specificationRepository = specificationRepository;
    }

    public RangeCheckResponse checkRange(
            String vehicleId,
            String userId,
            RangeCheckRequest request) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElse(null);

        if (vehicle == null) {
            throw new RuntimeException("Vehicle not found");
        }

        // Security: make sure this vehicle belongs to the logged-in user
        if (!vehicle.getUserId().equals(userId)) {
            throw new RuntimeException("You do not own this vehicle");
        }

        VehicleSpecification specification =
                specificationRepository.findById(
                        vehicle.getVehicleSpecificationId()
                ).orElse(null);

        if (specification == null) {
            throw new RuntimeException("Vehicle specification not found");
        }

        double estimatedRange =
                request.getCurrentEnergy()
                        * specification.getEfficiency();

        double distance =
                request.getDistanceToStation();

        boolean reachable =
                estimatedRange >= distance;

        double remainingRange =
                estimatedRange - distance;

        String message;

        if (reachable) {
            message = "Station is within your estimated range";
        } else {
            message = "Station is outside your estimated range";
        }

        return new RangeCheckResponse(
                estimatedRange,
                distance,
                reachable,
                remainingRange,
                message
        );
    }
}