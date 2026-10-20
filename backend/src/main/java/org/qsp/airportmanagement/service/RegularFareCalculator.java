package org.qsp.airportmanagement.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Default calculator: the customer pays the flight's listed price. */
@Service
@Primary
public class RegularFareCalculator implements FareCalculator {

    @Override
    public BigDecimal calculateFare(BigDecimal baseFare) {
        return baseFare;
    }
}
