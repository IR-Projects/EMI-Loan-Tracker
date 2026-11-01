package com.emitracker.domain.schedule;

import com.emitracker.domain.Money;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ScheduleTest {

    private static final Currency TRY = Currency.getInstance("TRY");
    private static final Currency USD = Currency.getInstance("USD");

    private static Money tl(long minor) {
        return Money.ofMinor(minor, TRY);
    }

    private static Installment installment(int number, LocalDate due, long principal, long interest, long balanceAfter) {
        return new Installment(number, due, tl(principal), tl(interest), tl(balanceAfter));
    }

    /** Hand-written two-installment schedule; no amortization maths involved. */
    private List<Installment> twoInstallments() {
        return List.of(
                installment(1, LocalDate.of(2026, 2, 15), 5_000, 100, 5_000),
                installment(2, LocalDate.of(2026, 3, 15), 5_000, 50, 0));
    }

    // ---- Installment ----

    @Test
    void installment_totalDue_isPrincipalPlusInterest() {
        Installment i = installment(1, LocalDate.of(2026, 2, 15), 5_000, 100, 5_000);

        assertEquals(tl(5_100), i.totalDue());
    }

    @Test
    void installment_rejectsNumberBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> installment(0, LocalDate.of(2026, 2, 15), 5_000, 100, 5_000));
    }

    @Test
    void installment_rejectsNegativeAmounts() {
        LocalDate due = LocalDate.of(2026, 2, 15);
        assertThrows(IllegalArgumentException.class, () -> installment(1, due, -1, 100, 5_000));
        assertThrows(IllegalArgumentException.class, () -> installment(1, due, 5_000, -1, 5_000));
        assertThrows(IllegalArgumentException.class, () -> installment(1, due, 5_000, 100, -1));
    }

    @Test
    void installment_rejectsMixedCurrencies() {
        assertThrows(IllegalArgumentException.class, () ->
                new Installment(1, LocalDate.of(2026, 2, 15),
                        tl(5_000), Money.ofMinor(100, USD), tl(5_000)));
    }

    @Test
    void installment_rejectsNullDueDate() {
        assertThrows(NullPointerException.class,
                () -> new Installment(1, null, tl(5_000), tl(100), tl(5_000)));
    }

    // ---- Schedule ----

    @Test
    void schedule_exposesInstallmentsAndTotals() {
        UUID loanId = UUID.randomUUID();

        Schedule schedule = new Schedule(loanId, twoInstallments());

        assertEquals(loanId, schedule.getLoanId());
        assertEquals(2, schedule.size());
        assertEquals(tl(10_000), schedule.totalPrincipal());
        assertEquals(tl(150), schedule.totalInterest());
    }

    @Test
    void schedule_rejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new Schedule(UUID.randomUUID(), List.of()));
    }

    @Test
    void schedule_rejectsGapsOrWrongStartInNumbering() {
        List<Installment> startsAtTwo = List.of(
                installment(2, LocalDate.of(2026, 2, 15), 5_000, 100, 5_000),
                installment(3, LocalDate.of(2026, 3, 15), 5_000, 50, 0));

        assertThrows(IllegalArgumentException.class, () -> new Schedule(UUID.randomUUID(), startsAtTwo));
    }

    @Test
    void schedule_rejectsNonIncreasingDueDates() {
        List<Installment> sameDate = List.of(
                installment(1, LocalDate.of(2026, 2, 15), 5_000, 100, 5_000),
                installment(2, LocalDate.of(2026, 2, 15), 5_000, 50, 0));

        assertThrows(IllegalArgumentException.class, () -> new Schedule(UUID.randomUUID(), sameDate));
    }

    @Test
    void schedule_rejectsMixedCurrenciesAcrossInstallments() {
        List<Installment> mixed = List.of(
                installment(1, LocalDate.of(2026, 2, 15), 5_000, 100, 5_000),
                new Installment(2, LocalDate.of(2026, 3, 15),
                        Money.ofMinor(5_000, USD), Money.ofMinor(50, USD), Money.zero(USD)));

        assertThrows(IllegalArgumentException.class, () -> new Schedule(UUID.randomUUID(), mixed));
    }

    @Test
    void schedule_isImmutable_evenIfCallerKeepsTheOriginalList() {
        List<Installment> source = new ArrayList<>(twoInstallments());
        Schedule schedule = new Schedule(UUID.randomUUID(), source);

        source.clear();

        assertEquals(2, schedule.size());
        assertThrows(UnsupportedOperationException.class,
                () -> schedule.getInstallments().add(installment(3, LocalDate.of(2026, 4, 15), 1, 1, 0)));
    }
}
