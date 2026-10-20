package org.qsp.airportmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookingRequest(
        @NotNull(message = "Flight ID is required")
        Long flightId,

        @NotEmpty(message = "At least one passenger is required")
        @Size(max = 9, message = "A booking can have at most 9 passengers")
        List<@Valid @NotNull(message = "Passenger details are required") PassengerRequest> passengers
) {}
