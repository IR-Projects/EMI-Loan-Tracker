package com.emitracker.domain.loan.state;

public final class ActiveState implements LoanState {
    public static final ActiveState INSTANCE = new ActiveState();
    private ActiveState() {}

    @Override public LoanStatus status() { return LoanStatus.ACTIVE; }

    @Override public LoanState onDueDateReached(LoanContext ctx) {
        return ctx.hasDueInstallment() ? DueState.INSTANCE : this;
    }

    @Override public LoanState onPaymentApplied(LoanContext ctx) {
        return ctx.isFullySettled() ? ClosedState.INSTANCE : this;
    }
}