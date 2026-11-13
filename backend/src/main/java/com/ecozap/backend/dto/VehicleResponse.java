package com.ecozap.backend.dto;

import com.ecozap.backend.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleResponse {

    private String id;
    private String vehicleNumber;

    private String brand;
    private String model;
    private VehicleType type;

    private Double batteryCapacity;
    private Double tankCapacity;
    private Double efficiency;
}