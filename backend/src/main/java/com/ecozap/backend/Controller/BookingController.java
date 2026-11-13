package com.ecozap.backend.Controller;

import com.ecozap.backend.dto.BookingRequest;
import com.ecozap.backend.dto.BookingResponse;
import com.ecozap.backend.model.user;
import com.ecozap.backend.repository.UserRepository;
import com.ecozap.backend.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(
            BookingService bookingService,
            UserRepository userRepository) {

        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // Create booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        user currentUser = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        BookingResponse response =
                bookingService.createBooking(
                        request,
                        currentUser.getId()
                );

        return ResponseEntity.ok(response);
    }

    // Cancel booking
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable String bookingId,
            Authentication authentication) {

        System.out.println("CANCEL ENDPOINT REACHED");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("User: " + authentication.getName());

        String email = authentication.getName();

        user currentUser = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        BookingResponse response =
                bookingService.cancelBooking(
                        bookingId,
                        currentUser.getId());

        return ResponseEntity.ok(response);
    }
}