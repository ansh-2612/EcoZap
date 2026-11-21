package com.ecozap.backend.dto;

import com.ecozap.backend.model.VehicleType;

public record SlotAvailability(String id, Integer slotNumber, String time, VehicleType vehicleType,
                               Double chargerPower, Integer estimatedServiceTimeMinutes,
                               boolean scheduled, boolean available) {}
