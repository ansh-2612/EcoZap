package com.ecozap.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class VehicleRequest {

    @NotBlank
    private String vehicleSpecificationId;

    @NotBlank
    private String vehicleNumber;
}