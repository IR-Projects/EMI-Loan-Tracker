package com.emitracker.domain.rate;

import java.time.LocalDate;
import java.util.Objects;

public final class FixedRateStrategy implements RateStrategy {

    private final int rateBps;

    public FixedRateStrategy(int rateBps) {
        if (rateBps < 0) {
            throw new IllegalArgumentException("rateBps must be >= 0");
        }
        this.rateBps = rateBps;
    }

    @Override
    public int rateBpsAt(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return rateBps;
    }

    public int getRateBps() {
        return rateBps;
    }
}