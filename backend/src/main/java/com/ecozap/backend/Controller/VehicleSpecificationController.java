package com.ecozap.backend.Controller;

import com.ecozap.backend.dto.VehicleSuggestionResponse;
import com.ecozap.backend.service.VehicleSpecificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-specifications")
public class VehicleSpecificationController {

    private final VehicleSpecificationService service;

    public VehicleSpecificationController(
            VehicleSpecificationService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<VehicleSuggestionResponse> search(
            @RequestParam String query) {

        return service.searchVehicles(query);
    }
}