package com.emitracker.domain.rate;

import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public final class FloatingRateStrategy implements RateStrategy {

    private final NavigableMap<LocalDate, Integer> ratesByEffectiveDate;

    public FloatingRateStrategy(Map<LocalDate, Integer> ratesByEffectiveDate) {
        Objects.requireNonNull(ratesByEffectiveDate, "ratesByEffectiveDate must not be null");
        if (ratesByEffectiveDate.isEmpty()) {
            throw new IllegalArgumentException("ratesByEffectiveDate must not be empty");
        }
        for (Map.Entry<LocalDate, Integer> entry : ratesByEffectiveDate.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "rate effective date must not be null");
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException("rate bps must be >= 0");
            }
        }
        this.ratesByEffectiveDate = new TreeMap<>(ratesByEffectiveDate);
    }

    @Override
    public int rateBpsAt(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        Map.Entry<LocalDate, Integer> entry = ratesByEffectiveDate.floorEntry(date);
        if (entry == null) {
            throw new IllegalArgumentException(
                    "No rate defined on or before " + date
            );
        }
        return entry.getValue();
    }

    public NavigableMap<LocalDate, Integer> getRatesByEffectiveDate() {
        return new TreeMap<>(ratesByEffectiveDate); // defensive copy
    }
}