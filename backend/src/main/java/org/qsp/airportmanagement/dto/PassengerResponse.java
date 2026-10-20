package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.Gender;

public record PassengerResponse(
        Long id,
        String name,
        int age,
        Gender gender
) {}
