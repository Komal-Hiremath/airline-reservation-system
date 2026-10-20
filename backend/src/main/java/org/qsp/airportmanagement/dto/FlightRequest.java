package org.qsp.airportmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record FlightRequest(
                @NotBlank(message = "Flight number is required") 
                @Size(max = 10, message = "Flight number cannot exceed 10 characters") 
                String flightNumber,

                @NotBlank(message = "Origin code is required") 
                @Pattern(regexp = "^[A-Za-z]{3}$", message = "Origin code must be exactly 3 letters") 
                String originCode,

                @NotBlank(message = "Destination code is required") 
                @Pattern(regexp = "^[A-Za-z]{3}$", message = "Destination code must be exactly 3 letters") 
                String destinationCode,

                @NotBlank String departureTime,

                @NotBlank String arrivalTime,

                @NotNull(message = "Price is required") 
                @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") 
                BigDecimal price,

                @NotNull(message = "Total seats is required") 
                @Min(value = 1, message = "Total seats must be at least 1") 
                Integer totalSeats) {
}
