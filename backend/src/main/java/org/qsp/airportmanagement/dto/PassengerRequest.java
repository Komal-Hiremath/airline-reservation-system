package org.qsp.airportmanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.qsp.airportmanagement.model.Gender;

public record PassengerRequest(
        @NotBlank(message = "Passenger name is required")
        @Size(max = 150, message = "Passenger name cannot exceed 150 characters")
        String name,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 120, message = "Age cannot be more than 120")
        Integer age,

        @NotNull(message = "Gender is required (MALE, FEMALE or OTHER)")
        Gender gender
) {}
