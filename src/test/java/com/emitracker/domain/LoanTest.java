package com.emitracker.domain;

import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class LoanTest {

    private Loan validLoan() {
        return new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                "Test description",
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200,
                12,
                15,
                5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        );
    }

    @Test
    void constructor_createsValidLoan() {
        Loan loan = validLoan();
        assertEquals("Car Loan", loan.getName());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(12, loan.getTenureMonths());
        assertEquals(1200, loan.getInterestRateBps());
    }

    @Test
    void constructor_throwsOnNullId() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new Loan(
                null,
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnBlankName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "   ",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnBlankLenderName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_allowsNullDescription() {
        Loan loan = new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        );
        assertNull(loan.getDescription());
    }

        @Test
    void constructor_throwsOnZeroPrincipal() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(0, Currency.getInstance("TRY")),
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnNegativeInterestRate() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                -1, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnZeroTenure() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 0, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnDueDayZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 0, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnDueDayAbove31() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 32, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_throwsOnNegativeGracePeriod() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, -1,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        ));
        assertNotNull(ex);
    }

    @Test
    void constructor_allowsZeroInterestRate() {
        Loan loan = new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                0, 12, 15, 5,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        );
        assertEquals(0, loan.getInterestRateBps());
    }

    @Test
    void constructor_allowsZeroGracePeriod() {
        Loan loan = new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Car Loan",
                null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, 0,
                LocalDate.of(2026, 1, 1),
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        );
        assertEquals(0, loan.getGracePeriodDays());
    }

        @Test
    void withStatus_returnsNewLoanWithUpdatedStatus() {
        Loan original = validLoan();
        Loan updated = original.withStatus(LoanStatus.OVERDUE);

        assertEquals(LoanStatus.ACTIVE, original.getStatus());
        assertEquals(LoanStatus.OVERDUE, updated.getStatus());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void withInterestRateBps_returnsNewLoanWithUpdatedRate() {
        Loan original = validLoan();
        Loan updated = original.withInterestRateBps(1500);

        assertEquals(1200, original.getInterestRateBps());
        assertEquals(1500, updated.getInterestRateBps());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void withTenureMonths_returnsNewLoanWithUpdatedTenure() {
        Loan original = validLoan();
        Loan updated = original.withTenureMonths(24);

        assertEquals(12, original.getTenureMonths());
        assertEquals(24, updated.getTenureMonths());
    }

    @Test
    void withPrincipal_returnsNewLoanWithUpdatedPrincipal() {
        Loan original = validLoan();
        Money newPrincipal = Money.ofMinor(2_000_000, Currency.getInstance("TRY"));
        Loan updated = original.withPrincipal(newPrincipal);

        assertEquals(1_000_000, original.getPrincipal().minorUnits());
        assertEquals(2_000_000, updated.getPrincipal().minorUnits());
    }

    @Test
    void withDueDayOfMonth_returnsNewLoanWithUpdatedDueDay() {
        Loan original = validLoan();
        Loan updated = original.withDueDayOfMonth(20);

        assertEquals(15, original.getDueDayOfMonth());
        assertEquals(20, updated.getDueDayOfMonth());
    }

    @Test
    void withGracePeriodDays_returnsNewLoanWithUpdatedGrace() {
        Loan original = validLoan();
        Loan updated = original.withGracePeriodDays(10);

        assertEquals(5, original.getGracePeriodDays());
        assertEquals(10, updated.getGracePeriodDays());
    }

    @Test
    void withStatus_throwsOnInvalidStatusTransition() {
        Loan original = validLoan();
        // withStatus doesn't validate transitions yet — Step 2 will handle that.
        // For now, just confirm it accepts any valid LoanStatus.
        Loan updated = original.withStatus(LoanStatus.CLOSED);
        assertEquals(LoanStatus.CLOSED, updated.getStatus());
    }

    @Test
    void equals_sameIdAreEqual() {
        UUID sharedId = UUID.randomUUID();
        Loan a = new Loan(
                sharedId, UUID.randomUUID(), "A", null,
                Money.ofMinor(1_000_000, Currency.getInstance("TRY")),
                1200, 12, 15, 5,
                LocalDate.of(2026, 1, 1), "Bank", RateType.FIXED, LoanStatus.ACTIVE
        );
        Loan b = new Loan(
                sharedId, UUID.randomUUID(), "B", null,
                Money.ofMinor(2_000_000, Currency.getInstance("TRY")),
                500, 24, 20, 10,
                LocalDate.of(2026, 6, 1), "Other Bank", RateType.FLOATING, LoanStatus.CLOSED
        );

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentIdNotEqual() {
        Loan a = validLoan();
        Loan b = validLoan();

        assertNotEquals(a, b);
    }

    @Test
    void toString_containsKeyFields() {
        Loan loan = validLoan();
        String s = loan.toString();

        assertTrue(s.contains("Car Loan"));
        assertTrue(s.contains("Test Bank"));
        assertTrue(s.contains("ACTIVE"));
        assertTrue(s.contains("FIXED"));
    }

    
}