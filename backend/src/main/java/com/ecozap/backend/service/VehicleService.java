package com.ecozap.backend.service;

import com.ecozap.backend.dto.VehicleRequest;
import com.ecozap.backend.dto.VehicleResponse;
import com.ecozap.backend.model.Vehicle;
import com.ecozap.backend.model.VehicleSpecification;
import com.ecozap.backend.repository.VehicleRepository;
import com.ecozap.backend.repository.VehicleSpecificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleSpecificationRepository specificationRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            VehicleSpecificationRepository specificationRepository) {

        this.vehicleRepository = vehicleRepository;
        this.specificationRepository = specificationRepository;
    }

    public Vehicle addVehicle(VehicleRequest request, String userId) {

        VehicleSpecification specification =
                specificationRepository.findById(
                        request.getVehicleSpecificationId()
                ).orElse(null);

        if (specification == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Vehicle specification not found");
        }

        Vehicle vehicle = new Vehicle();

        vehicle.setUserId(userId);
        vehicle.setVehicleSpecificationId(
                request.getVehicleSpecificationId()
        );
        vehicle.setVehicleNumber(
                request.getVehicleNumber()
        );

        return vehicleRepository.save(vehicle);
    }

    public List<VehicleResponse> getUserVehicles(String userId) {

        List<Vehicle> vehicles =
                vehicleRepository.findByUserId(userId);

        return vehicles.stream()
                .map(vehicle -> {

                    VehicleSpecification specification =
                            specificationRepository.findById(
                                    vehicle.getVehicleSpecificationId()
                            ).orElse(null);

                    if (specification == null) {
                        return null;
                    }

                    return new VehicleResponse(
                            vehicle.getId(),
                            vehicle.getVehicleNumber(),
                            specification.getBrand(),
                            specification.getModel(),
                            specification.getType(),
                            specification.getBatteryCapacity(),
                            specification.getTankCapacity(),
                            specification.getEfficiency()
                    );
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
}