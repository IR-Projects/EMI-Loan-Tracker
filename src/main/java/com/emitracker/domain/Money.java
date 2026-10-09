package com.emitracker.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money implements Comparable<Money> {

    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private static final BigDecimal BPS_DIVISOR = BigDecimal.valueOf(10_000);

    private final long minorUnits;
    private final Currency currency;

    private Money(long minorUnits, Currency currency) {
        this.minorUnits = minorUnits;
        this.currency = Objects.requireNonNull(currency, "currency null olamaz");
    }

    public static Money ofMinor(long minorUnits, Currency currency) {
        return new Money(minorUnits, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public long minorUnits() {
        return minorUnits;
    }

    public Currency currency() {
        return currency;
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(minorUnits, other.minorUnits), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.subtractExact(minorUnits, other.minorUnits), currency);
    }

    public Money negate() {
        return new Money(Math.negateExact(minorUnits), currency);
    }
 
 
    public Money multiplyByBps(long bps) {
        BigDecimal result = BigDecimal.valueOf(minorUnits)
                .multiply(BigDecimal.valueOf(bps))
                .divide(BPS_DIVISOR, 0, ROUNDING);
        return new Money(result.longValueExact(), currency);
    }



    public boolean isZero() {
        return minorUnits == 0;
    }

    public boolean isPositive() {
        return minorUnits > 0;
    }

    public boolean isNegative() {
        return minorUnits < 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other null olamaz");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Para birimleri farklı: " + currency + " ve " + other.currency);
        }
    }


    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return Long.compare(minorUnits, other.minorUnits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return minorUnits == m.minorUnits && currency.equals(m.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minorUnits, currency);
    }

    @Override
    public String toString() {
        int digits = Math.max(currency.getDefaultFractionDigits(), 0);
        BigDecimal major = BigDecimal.valueOf(minorUnits, digits);
        return major.toPlainString() + " " + currency.getCurrencyCode();
    }
}
