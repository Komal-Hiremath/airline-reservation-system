package org.qsp.airportmanagement.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Festival season calculator (+20%). Select it with @Qualifier("festivalFare"). */
@Service("festivalFare")
public class FestivalFareCalculator implements FareCalculator {

    private static final BigDecimal FESTIVAL_MULTIPLIER = new BigDecimal("1.20");

    @Override
    public BigDecimal calculateFare(BigDecimal baseFare) {
        return baseFare.multiply(FESTIVAL_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
    }
}
