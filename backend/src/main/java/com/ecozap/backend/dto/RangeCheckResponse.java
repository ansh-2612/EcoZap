package com.ecozap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RangeCheckResponse {

    private Double estimatedRange;
    private Double distanceToStation;
    private Boolean reachable;
    private Double remainingRangeAfterTrip;
    private String message;
}