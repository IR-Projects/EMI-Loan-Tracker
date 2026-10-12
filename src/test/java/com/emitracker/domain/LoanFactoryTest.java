package com.emitracker.domain;

import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class LoanFactoryTest {

    private final LoanFactory factory = new LoanFactory();

    @Test
    void createFixedRateLoan_setsFixedTypeAndActiveStatus() {
        UUID userId = UUID.randomUUID();
        Money principal = Money.ofMinor(1_000_000, Currency.getInstance("TRY"));

        Loan loan = factory.createFixedRateLoan(
                userId, "Car Loan", "desc", principal,
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1), "Test Bank"
        );

        assertEquals(RateType.FIXED, loan.getRateType());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(1200, loan.getInterestRateBps());
        assertEquals(userId, loan.getUserId());
        assertNotNull(loan.getId());
    }

    @Test
    void createFloatingRateLoan_setsFloatingTypeZeroRateAndActiveStatus() {
        UUID userId = UUID.randomUUID();
        Money principal = Money.ofMinor(1_000_000, Currency.getInstance("TRY"));

        Loan loan = factory.createFloatingRateLoan(
                userId, "Car Loan", "desc", principal,
                12, 15, 5,
                LocalDate.of(2026, 1, 1), "Test Bank"
        );

        assertEquals(RateType.FLOATING, loan.getRateType());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(0, loan.getInterestRateBps());
        assertEquals(userId, loan.getUserId());
        assertNotNull(loan.getId());
    }

    @Test
    void createLoan_generatesUniqueIds() {
        UUID userId = UUID.randomUUID();
        Money principal = Money.ofMinor(1_000_000, Currency.getInstance("TRY"));

        Loan a = factory.createFixedRateLoan(
                userId, "A", null, principal,
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1), "Test Bank"
        );
        Loan b = factory.createFixedRateLoan(
                userId, "B", null, principal,
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1), "Test Bank"
        );

        assertNotEquals(a.getId(), b.getId());
        assertNotEquals(a, b);
    }

    @Test
    void createLoan_throwsOnNullUserId() {
        Money principal = Money.ofMinor(1_000_000, Currency.getInstance("TRY"));

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                factory.createFixedRateLoan(
                        null, "Car Loan", null, principal,
                        1200, 12, 15, 5,
                        LocalDate.of(2026, 1, 1), "Test Bank"
                )
        );
        assertNotNull(ex);
    }

    @Test
    void createLoan_propagatesValidationFromLoanConstructor() {
        UUID userId = UUID.randomUUID();
        Money principal = Money.ofMinor(1_000_000, Currency.getInstance("TRY"));

        // Invalid tenure (0) should be rejected by Loan's constructor.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                factory.createFixedRateLoan(
                        userId, "Car Loan", null, principal,
                        1200, 0, 15, 5,
                        LocalDate.of(2026, 1, 1), "Test Bank"
                )
        );
        assertNotNull(ex);
    }
}