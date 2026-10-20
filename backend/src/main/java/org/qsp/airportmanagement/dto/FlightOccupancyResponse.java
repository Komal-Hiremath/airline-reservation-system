package org.qsp.airportmanagement.dto;

public record FlightOccupancyResponse(
        Long flightId,
        String flightNumber,
        int totalSeats,
        long bookedSeats,
        long availableSeats,
        double occupancyPercentage
) {}
