package org.qsp.airportmanagement.dto;

import java.math.BigDecimal;

public record FlightResponse(
        Long id,
        String flightNumber,
        AirportResponse origin,
        AirportResponse destination,
        String departureTime,
        String arrivalTime,
        BigDecimal price,
        int totalSeats,
        int availableSeats,
        boolean active
) {}
