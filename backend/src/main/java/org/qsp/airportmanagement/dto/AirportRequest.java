package org.qsp.airportmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AirportRequest(
        @NotBlank(message = "Airport code is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Airport code must be exactly 3 letters")
        String code,

        @NotBlank(message = "Airport name is required")
        @Size(max = 150, message = "Airport name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city
) {}
