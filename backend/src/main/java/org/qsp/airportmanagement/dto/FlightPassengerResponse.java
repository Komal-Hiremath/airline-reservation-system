package org.qsp.airportmanagement.dto;

public record FlightPassengerResponse(
        Long passengerId,
        String name,
        int age,
        String gender,
        Long bookingId,
        String bookedBy
) {}
