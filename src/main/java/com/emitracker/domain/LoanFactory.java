package com.emitracker.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class LoanFactory {

    public LoanFactory() {
        // Public so Spring (later) and unit tests can instantiate it.
    }

    public Loan createFixedRateLoan(
            UUID userId,
            String name,
            String description,
            Money principal,
            int interestRateBps,
            int tenureMonths,
            int dueDayOfMonth,
            int gracePeriodDays,
            LocalDate startDate,
            String lenderName) {

        return build(
                userId, name, description, principal,
                interestRateBps, tenureMonths, dueDayOfMonth, gracePeriodDays,
                startDate, lenderName, RateType.FIXED
        );
    }

    public Loan createFloatingRateLoan(
            UUID userId,
            String name,
            String description,
            Money principal,
            int tenureMonths,
            int dueDayOfMonth,
            int gracePeriodDays,
            LocalDate startDate,
            String lenderName) {

        return build(
                userId, name, description, principal,
                0, tenureMonths, dueDayOfMonth, gracePeriodDays,
                startDate, lenderName, RateType.FLOATING
        );
    }

    private Loan build(
            UUID userId,
            String name,
            String description,
            Money principal,
            int interestRateBps,
            int tenureMonths,
            int dueDayOfMonth,
            int gracePeriodDays,
            LocalDate startDate,
            String lenderName,
            RateType rateType) {

        Objects.requireNonNull(userId, "userId must not be null");

        // The factory decides *what kind* of loan to make (rate type, starting status);
        // the builder takes care of assembling the many fields.
        return Loan.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .principal(principal)
                .interestRateBps(interestRateBps)
                .tenureMonths(tenureMonths)
                .dueDayOfMonth(dueDayOfMonth)
                .gracePeriodDays(gracePeriodDays)
                .startDate(startDate)
                .lenderName(lenderName)
                .rateType(rateType)
                .status(LoanStatus.ACTIVE)
                .build();
    }
}