package com.ecozap.backend.repository;

import com.ecozap.backend.model.Booking;
import com.ecozap.backend.model.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByUserIdOrderByBookingTimeDesc(String userId);

    List<Booking> findBySlotId(String slotId);

    Optional<Booking> findBySlotIdAndStatus(
            String slotId,
            BookingStatus status
    );
}