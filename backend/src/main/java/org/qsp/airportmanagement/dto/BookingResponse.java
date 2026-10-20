package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        BookingStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt,
        FlightResponse flight,
        List<PassengerResponse> passengers
) {}
