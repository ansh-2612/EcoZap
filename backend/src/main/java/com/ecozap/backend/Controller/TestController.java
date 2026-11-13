package com.ecozap.backend.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/protected")
    public String protectedEndpoint(Authentication authentication) {

        System.out.println("===== CONTROLLER REACHED =====");
        System.out.println("Authentication: " + authentication);
        System.out.println("Authenticated: " + authentication.isAuthenticated());
        System.out.println("Name: " + authentication.getName());

        return "Authenticated as: " + authentication.getName();
    }
}