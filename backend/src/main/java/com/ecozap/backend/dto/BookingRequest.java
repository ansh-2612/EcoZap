package com.ecozap.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookingRequest {

    @NotBlank
    private String vehicleId;

    @NotBlank
    private String stationId;

    @NotBlank
    private String slotId;

    // Required for EV booking
    @Positive
    private Double currentEnergy;

    // Required for EV charging target
    @Positive
    private Double targetEnergy;
}