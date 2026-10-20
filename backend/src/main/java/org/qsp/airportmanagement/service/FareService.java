package org.qsp.airportmanagement.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Used by BookingService to price a booking.
 * The @Primary calculator (Regular) is injected. To switch to festival pricing,
 * add @Qualifier("festivalFare") to the constructor parameter.
 */
@Service
public class FareService {

    private final FareCalculator fareCalculator;

    public FareService(FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    public BigDecimal totalFor(BigDecimal basePrice, int passengerCount) {
        return fareCalculator.calculateFare(basePrice)
                .multiply(BigDecimal.valueOf(passengerCount));
    }
}
