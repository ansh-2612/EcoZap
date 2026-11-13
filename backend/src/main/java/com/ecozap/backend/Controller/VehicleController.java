

package com.ecozap.backend.Controller;
import com.ecozap.backend.dto.VehicleResponse;
import com.ecozap.backend.dto.VehicleRequest;
import com.ecozap.backend.model.Vehicle;
import com.ecozap.backend.model.user;
import com.ecozap.backend.repository.UserRepository;
import com.ecozap.backend.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserRepository userRepository;

    public VehicleController(
            VehicleService vehicleService,
            UserRepository userRepository) {

        this.vehicleService = vehicleService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> addVehicle(
            @Valid @RequestBody VehicleRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        user currentUser = userRepository.findByEmail(email)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404)
                    .body("User not found");
        }

        Vehicle vehicle = vehicleService.addVehicle(
                request,
                currentUser.getId()
        );

        return ResponseEntity.ok(vehicle);
    }

    @GetMapping
    public ResponseEntity<?> getMyVehicles(
            Authentication authentication) {

        String email = authentication.getName();

        user currentUser = userRepository.findByEmail(email)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404)
                    .body("User not found");
        }

        List<VehicleResponse> vehicles =
                vehicleService.getUserVehicles(
                        currentUser.getId()
                );

        return ResponseEntity.ok(vehicles);
    }
}