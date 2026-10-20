package org.qsp.airportmanagement.service;

import java.math.BigDecimal;

/** Strategy for turning a flight's base price into the price charged per passenger. */
public interface FareCalculator {

    BigDecimal calculateFare(BigDecimal baseFare);
}
