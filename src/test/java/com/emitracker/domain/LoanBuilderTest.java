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

class LoanBuilderTest {

    private static final Currency TRY = Currency.getInstance("TRY");

    /** A builder with every required field set to a valid value. */
    private LoanBuilder validBuilder() {
        return Loan.builder()
                .userId(UUID.randomUUID())
                .name("Car Loan")
                .principal(Money.ofMinor(1_000_000, TRY))
                .interestRateBps(1200)
                .tenureMonths(12)
                .dueDayOfMonth(15)
                .startDate(LocalDate.of(2026, 1, 1))
                .lenderName("Test Bank")
                .rateType(RateType.FIXED);
    }

    @Test
    void build_withAllRequiredFields_createsLoanWithGivenValues() {
        UUID userId = UUID.randomUUID();

        Loan loan = validBuilder()
                .userId(userId)
                .description("For the new car")
                .gracePeriodDays(5)
                .build();

        assertEquals(userId, loan.getUserId());
        assertEquals("Car Loan", loan.getName());
        assertEquals("For the new car", loan.getDescription());
        assertEquals(Money.ofMinor(1_000_000, TRY), loan.getPrincipal());
        assertEquals(1200, loan.getInterestRateBps());
        assertEquals(12, loan.getTenureMonths());
        assertEquals(15, loan.getDueDayOfMonth());
        assertEquals(5, loan.getGracePeriodDays());
        assertEquals(LocalDate.of(2026, 1, 1), loan.getStartDate());
        assertEquals("Test Bank", loan.getLenderName());
        assertEquals(RateType.FIXED, loan.getRateType());
    }

    @Test
    void build_appliesDefaults_forIdDescriptionGraceAndStatus() {
        Loan loan = validBuilder().build();

        assertNotNull(loan.getId());
        assertNull(loan.getDescription());
        assertEquals(0, loan.getGracePeriodDays());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    void build_generatesDifferentIdsForSeparateBuilds() {
        Loan a = validBuilder().build();
        Loan b = validBuilder().build();

        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void build_honoursExplicitIdAndStatus() {
        UUID id = UUID.randomUUID();

        Loan loan = validBuilder().id(id).status(LoanStatus.OVERDUE).build();

        assertEquals(id, loan.getId());
        assertEquals(LoanStatus.OVERDUE, loan.getStatus());
    }

    @Test
    void build_throwsIllegalState_whenTenureNotSet() {
        LoanBuilder builder = Loan.builder()
                .userId(UUID.randomUUID())
                .name("Car Loan")
                .principal(Money.ofMinor(1_000_000, TRY))
                .interestRateBps(1200)
                .dueDayOfMonth(15)
                .startDate(LocalDate.of(2026, 1, 1))
                .lenderName("Test Bank")
                .rateType(RateType.FIXED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().contains("tenureMonths"));
    }

    @Test
    void build_throwsIllegalState_whenInterestRateNotSet() {
        LoanBuilder builder = Loan.builder()
                .userId(UUID.randomUUID())
                .name("Car Loan")
                .principal(Money.ofMinor(1_000_000, TRY))
                .tenureMonths(12)
                .dueDayOfMonth(15)
                .startDate(LocalDate.of(2026, 1, 1))
                .lenderName("Test Bank")
                .rateType(RateType.FIXED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().contains("interestRateBps"));
    }

    @Test
    void build_throwsIllegalState_whenDueDayNotSet() {
        LoanBuilder builder = Loan.builder()
                .userId(UUID.randomUUID())
                .name("Car Loan")
                .principal(Money.ofMinor(1_000_000, TRY))
                .interestRateBps(1200)
                .tenureMonths(12)
                .startDate(LocalDate.of(2026, 1, 1))
                .lenderName("Test Bank")
                .rateType(RateType.FIXED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().contains("dueDayOfMonth"));
    }

    @Test
    void build_stillRejectsInvalidValues_viaLoanConstructor() {
        assertThrows(IllegalArgumentException.class, () -> validBuilder().tenureMonths(0).build());
        assertThrows(IllegalArgumentException.class, () -> validBuilder().dueDayOfMonth(32).build());
        assertThrows(IllegalArgumentException.class, () -> validBuilder().name(" ").build());
        assertThrows(IllegalArgumentException.class,
                () -> validBuilder().principal(Money.zero(TRY)).build());
    }

    @Test
    void build_throwsNullPointer_whenUserIdMissing() {
        assertThrows(NullPointerException.class, () -> validBuilder().userId(null).build());
    }
}
