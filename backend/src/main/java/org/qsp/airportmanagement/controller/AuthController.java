package org.qsp.airportmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.qsp.airportmanagement.dto.AuthResponse;
import org.qsp.airportmanagement.dto.LoginRequest;
import org.qsp.airportmanagement.dto.RegisterRequest;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.dto.UserResponse;
import org.qsp.airportmanagement.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a customer account")
    @PostMapping("/register")
    public ResponseEntity<ResponseStructure<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        ResponseStructure<UserResponse> response = authService.register(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Log in and receive a JWT")
    @PostMapping("/login")
    public ResponseEntity<ResponseStructure<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        ResponseStructure<AuthResponse> response = authService.login(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
