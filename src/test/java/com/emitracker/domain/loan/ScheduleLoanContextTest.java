package com.emitracker.domain.loan;

import com.emitracker.domain.Money;
import com.emitracker.domain.loan.state.ActiveState;
import com.emitracker.domain.loan.state.ClosedState;
import com.emitracker.domain.loan.state.DelinquentState;
import com.emitracker.domain.loan.state.DueState;
import com.emitracker.domain.loan.state.OverdueState;
import com.emitracker.domain.schedule.Installment;
import com.emitracker.domain.schedule.Schedule;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Drives the real State classes through a real (hand-written) schedule.
 * Installments fall due on 15 Feb, 15 Mar and 15 Apr 2026. Grace period 5 days, so an
 * installment is overdue from the 6th day after its due date; delinquency threshold 30 days.
 */
class ScheduleLoanContextTest {

    private static final Currency TRY = Currency.getInstance("TRY");
    private static final int GRACE = 5;
    private static final int THRESHOLD = 30;

    private static Money tl(long minor) {
        return Money.ofMinor(minor, TRY);
    }

    private static Schedule threeInstallments() {
        return new Schedule(UUID.randomUUID(), List.of(
                new Installment(1, LocalDate.of(2026, 2, 15), tl(3_000), tl(100), tl(6_000)),
                new Installment(2, LocalDate.of(2026, 3, 15), tl(3_000), tl(60), tl(3_000)),
                new Installment(3, LocalDate.of(2026, 4, 15), tl(3_000), tl(30), tl(0))));
    }

    private ScheduleLoanContext newContext() {
        return new ScheduleLoanContext(threeInstallments(), GRACE, THRESHOLD, LocalDate.of(2026, 1, 20));
    }

    // ---- time-driven transitions ----

    @Test
    void startsActive_andStaysActiveBeforeFirstDueDate() {
        ScheduleLoanContext ctx = newContext();

        ctx.advanceTo(LocalDate.of(2026, 2, 14));

        assertSame(ActiveState.INSTANCE, ctx.getState());
        assertTrue(ctx.getAuditTrail().isEmpty());
    }

    @Test
    void becomesDue_onTheDueDate() {
        ScheduleLoanContext ctx = newContext();

        ctx.advanceTo(LocalDate.of(2026, 2, 15));

        assertSame(DueState.INSTANCE, ctx.getState());
    }

    @Test
    void staysDue_onTheLastDayOfGrace_andBecomesOverdueTheDayAfter() {
        ScheduleLoanContext ctx = newContext();

        ctx.advanceTo(LocalDate.of(2026, 2, 20)); // 15 Feb + 5 days grace
        assertSame(DueState.INSTANCE, ctx.getState());

        ctx.advanceTo(LocalDate.of(2026, 2, 21));
        assertSame(OverdueState.INSTANCE, ctx.getState());
    }

    @Test
    void becomesDelinquent_oncePastTheThreshold() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 21));

        ctx.advanceTo(LocalDate.of(2026, 3, 16)); // 29 days past 15 Feb
        assertSame(OverdueState.INSTANCE, ctx.getState());

        ctx.advanceTo(LocalDate.of(2026, 3, 17)); // 30 days past 15 Feb
        assertSame(DelinquentState.INSTANCE, ctx.getState());
    }

    @Test
    void longGapWithoutLooking_stillWalksActiveToDelinquent_andRecordsEachStep() {
        ScheduleLoanContext ctx = newContext();

        ctx.advanceTo(LocalDate.of(2026, 3, 20)); // 33 days after the first due date

        assertSame(DelinquentState.INSTANCE, ctx.getState());
        List<ScheduleLoanContext.Transition> trail = ctx.getAuditTrail();
        assertEquals(3, trail.size());
        assertEquals("DUE_DATE_REACHED", trail.get(0).event());
        assertSame(ActiveState.INSTANCE, trail.get(0).from());
        assertSame(DueState.INSTANCE, trail.get(0).to());
        assertEquals("GRACE_EXPIRED", trail.get(1).event());
        assertSame(OverdueState.INSTANCE, trail.get(1).to());
        assertEquals("DELINQUENCY_THRESHOLD_REACHED", trail.get(2).event());
        assertSame(DelinquentState.INSTANCE, trail.get(2).to());
    }

    // ---- payment-driven transitions ----

    @Test
    void payingTheOverdueInstallment_returnsLoanToActive_whenNothingElseIsDue() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 25));
        assertSame(OverdueState.INSTANCE, ctx.getState());

        ctx.payInstallment(1);

        assertSame(ActiveState.INSTANCE, ctx.getState());
    }

    @Test
    void payingTheOldestWhileTheNextIsDue_leavesLoanDue() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 3, 20)); // installment 1 delinquent, installment 2 due (in grace)
        assertSame(DelinquentState.INSTANCE, ctx.getState());

        ctx.payInstallment(1);

        assertSame(DueState.INSTANCE, ctx.getState());
    }

    @Test
    void payingEverything_closesTheLoan_andClosedIsTerminal() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 16));

        ctx.payInstallment(1);
        ctx.payInstallment(2);
        ctx.payInstallment(3);
        assertSame(ClosedState.INSTANCE, ctx.getState());

        ctx.advanceTo(LocalDate.of(2027, 1, 1));
        assertSame(ClosedState.INSTANCE, ctx.getState());
    }

    // ---- idempotency ----

    @Test
    void payingTheSameInstallmentTwice_causesNoSecondTransition() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 25));

        ctx.payInstallment(1);
        int transitionsAfterFirst = ctx.getAuditTrail().size();
        ctx.payInstallment(1);

        assertEquals(transitionsAfterFirst, ctx.getAuditTrail().size());
        assertSame(ActiveState.INSTANCE, ctx.getState());
    }

    @Test
    void advancingToTheSameDateTwice_changesNothing() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 25));
        int transitions = ctx.getAuditTrail().size();

        ctx.advanceTo(LocalDate.of(2026, 2, 25));

        assertEquals(transitions, ctx.getAuditTrail().size());
    }

    // ---- the facts the states ask for ----

    @Test
    void facts_matchTheScheduleAndTheCalendar() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 18)); // installment 1: due, inside grace

        assertTrue(ctx.hasDueInstallment());
        assertFalse(ctx.hasOverdueInstallment());
        assertFalse(ctx.isDelinquencyThresholdReached());
        assertFalse(ctx.isFullySettled());
    }

    // ---- guards ----

    @Test
    void rejectsTimeGoingBackwards() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 25));

        assertThrows(IllegalArgumentException.class, () -> ctx.advanceTo(LocalDate.of(2026, 2, 24)));
    }

    @Test
    void rejectsUnknownInstallmentNumbers() {
        ScheduleLoanContext ctx = newContext();

        assertThrows(IllegalArgumentException.class, () -> ctx.payInstallment(0));
        assertThrows(IllegalArgumentException.class, () -> ctx.payInstallment(4));
    }

    @Test
    void rejectsThresholdSmallerThanGrace() {
        assertThrows(IllegalArgumentException.class, () ->
                new ScheduleLoanContext(threeInstallments(), 10, 5, LocalDate.of(2026, 1, 20)));
    }

    @Test
    void auditTrailIsReadOnly() {
        ScheduleLoanContext ctx = newContext();
        ctx.advanceTo(LocalDate.of(2026, 2, 15));

        assertThrows(UnsupportedOperationException.class, () -> ctx.getAuditTrail().clear());
    }
}
