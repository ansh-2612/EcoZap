package com.ecozap.backend.dto;

import com.ecozap.backend.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleSuggestionResponse {

    private String id;
    private String brand;
    private String model;
    private VehicleType type;
}