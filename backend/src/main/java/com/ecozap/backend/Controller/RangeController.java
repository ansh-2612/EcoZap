package com.ecozap.backend.Controller;

import com.ecozap.backend.dto.RangeCheckRequest;
import com.ecozap.backend.dto.RangeCheckResponse;
import com.ecozap.backend.model.user;
import com.ecozap.backend.repository.UserRepository;
import com.ecozap.backend.service.RangeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/range")
public class RangeController {

    private final RangeService rangeService;
    private final UserRepository userRepository;

    public RangeController(
            RangeService rangeService,
            UserRepository userRepository) {

        this.rangeService = rangeService;
        this.userRepository = userRepository;
    }

    @PostMapping("/check/{vehicleId}")
    public ResponseEntity<?> checkRange(
            @PathVariable String vehicleId,
            @Valid @RequestBody RangeCheckRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        user currentUser = userRepository.findByEmail(email)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404)
                    .body("User not found");
        }

        RangeCheckResponse response =
                rangeService.checkRange(
                        vehicleId,
                        currentUser.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}