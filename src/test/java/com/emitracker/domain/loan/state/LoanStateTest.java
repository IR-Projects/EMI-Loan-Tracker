package com.emitracker.domain.loan.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoanStateTest {

    @Test void activeToDueWhenDueDateReached() {
        FakeLoanContext ctx = new FakeLoanContext();
        ctx.setDue(true);
        assertEquals(DueState.INSTANCE, ctx.getState().onDueDateReached(ctx));
    }

    @Test void activeStaysActiveIfNoDueInstallment() {
        FakeLoanContext ctx = new FakeLoanContext();
        ctx.setDue(false);
        assertSame(ActiveState.INSTANCE, ctx.getState().onDueDateReached(ctx));
    }

    @Test void dueToOverdueWhenGraceExpired() {
        FakeLoanContext ctx = new FakeLoanContext();
        ctx.setState(DueState.INSTANCE);
        ctx.setOverdue(true);
        assertEquals(OverdueState.INSTANCE, ctx.getState().onGraceExpired(ctx));
    }

    @Test void overdueToDelinquentWhenThresholdReached() {
        FakeLoanContext ctx = new FakeLoanContext();
        ctx.setState(OverdueState.INSTANCE);
        ctx.setDelinquent(true);
        assertEquals(DelinquentState.INSTANCE, ctx.getState().onDelinquencyThresholdReached(ctx));
    }

    @Test void paymentAppliedClosesLoanWhenFullySettled() {
        FakeLoanContext ctx = new FakeLoanContext();
        ctx.setState(OverdueState.INSTANCE);
        ctx.setSettled(true);
        assertEquals(ClosedState.INSTANCE, ctx.getState().onPaymentApplied(ctx));
    }

    @Test void paymentAppliedRevertsToActiveWhenNoDuesLeft() {
        FakeLoanContext ctx = new FakeLoanContext();
        ctx.setState(DueState.INSTANCE);
        ctx.setDue(false);
        ctx.setOverdue(false);
        ctx.setSettled(false);
        assertEquals(ActiveState.INSTANCE, ctx.getState().onPaymentApplied(ctx));
    }
}