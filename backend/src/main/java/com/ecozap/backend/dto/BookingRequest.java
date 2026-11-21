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

    private java.time.LocalDate serviceDate;
    private java.util.UUID requestId;

    // kWh, including a depleted battery
    @jakarta.validation.constraints.PositiveOrZero
    private Double currentEnergy;

    // Required for EV charging target
    @Positive
    private Double targetEnergy;
}