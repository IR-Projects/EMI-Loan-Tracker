package com.emitracker.domain.loan.state;

public final class OverdueState implements LoanState {
    public static final OverdueState INSTANCE = new OverdueState();
    private OverdueState() {}

    @Override public LoanStatus status() { return LoanStatus.OVERDUE; }

    @Override public LoanState onDelinquencyThresholdReached(LoanContext ctx) {
        return ctx.isDelinquencyThresholdReached() ? DelinquentState.INSTANCE : this;
    }

    @Override public LoanState onPaymentApplied(LoanContext ctx) {
        if (ctx.isFullySettled()) return ClosedState.INSTANCE;
        if (!ctx.hasOverdueInstallment()) {
            return ctx.hasDueInstallment() ? DueState.INSTANCE : ActiveState.INSTANCE;
        }
        return ctx.isDelinquencyThresholdReached()
                ? DelinquentState.INSTANCE
                : this;
    }
}