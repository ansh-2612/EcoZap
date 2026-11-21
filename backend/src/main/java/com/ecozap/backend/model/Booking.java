package com.ecozap.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    private String userId;
    private String vehicleId;
    private String stationId;
    private String slotId;
    private String reservationKey;
    private java.time.LocalDate requestedDate;
    private Double requestedCurrentEnergy;
    private Double requestedTargetEnergy;

    private LocalDateTime bookingTime;
    private LocalDateTime arrivalTime;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;

    private BookingStatus status;
}