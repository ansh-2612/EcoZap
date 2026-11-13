package com.ecozap.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vehicle_specifications")
public class VehicleSpecification {

    @Id
    private String id;

    private String brand;

    private String model;

    private VehicleType type;

    // EV: battery capacity in kWh
    private Double batteryCapacity;

    // CNG: tank capacity in kg
    private Double tankCapacity;

    // EV: km per kWh
    // CNG: km per kg
    private Double efficiency;
}