package com.ecozap.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RangeCheckRequest {

    @NotNull
    @Positive
    private Double currentEnergy;

    @NotNull
    @Positive
    private Double distanceToStation;
}