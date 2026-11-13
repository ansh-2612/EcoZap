package com.ecozap.backend.dto;

import com.ecozap.backend.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {

    private String bookingId;
    private String vehicleId;
    private String stationId;
    private String slotId;

    private LocalDateTime bookingTime;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;

    private BookingStatus status;
}