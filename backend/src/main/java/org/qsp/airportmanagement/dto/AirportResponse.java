package org.qsp.airportmanagement.dto;

public record AirportResponse(
        Long id,
        String code,
        String name,
        String city,
        boolean active
) {}
