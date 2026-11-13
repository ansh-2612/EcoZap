package com.ecozap.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "station_slots")
public class StationSlot {

    @Id
    private String id;

    private String stationId;

    private Integer slotNumber;

    private String time;

    private VehicleType vehicleType;

    // EV charging power in kW
    private Double chargerPower;

    // Estimated service time for CNG in minutes
    private Integer estimatedServiceTimeMinutes;
}