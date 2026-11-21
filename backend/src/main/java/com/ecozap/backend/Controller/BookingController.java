package com.ecozap.backend.Controller;

import com.ecozap.backend.dto.*;
import com.ecozap.backend.repository.UserRepository;
import com.ecozap.backend.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookings;
    private final UserRepository users;

    public BookingController(BookingService bookings, UserRepository users) {
        this.bookings = bookings;
        this.users = users;
    }

    @GetMapping
    public List<BookingResponse> history(Authentication auth) {
        return bookings.history(userId(auth));
    }

    @PostMapping
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request, Authentication auth) {
        return bookings.createBooking(request, userId(auth));
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable String bookingId, Authentication auth) {
        return bookings.cancelBooking(bookingId, userId(auth));
    }

    private String userId(Authentication auth) {
        return users.findByEmail(auth.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again")).getId();
    }
}
