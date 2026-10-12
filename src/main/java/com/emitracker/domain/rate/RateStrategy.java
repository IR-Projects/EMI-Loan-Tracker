package com.emitracker.domain.rate;

import java.time.LocalDate;

public interface RateStrategy {

    /**
     * Returns the interest rate in basis points that applies on the given date.
     *
     * @param date the date to look up (typically an installment due date)
     * @return rate in basis points, e.g. 1200 = 12.00%
     * @throws IllegalArgumentException if no rate is defined for the given date
     */
    int rateBpsAt(LocalDate date);
}