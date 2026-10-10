package com.emitracker.domain.loan.state;

public final class DelinquentState implements LoanState {
    public static final DelinquentState INSTANCE = new DelinquentState();
    private DelinquentState() {}

    @Override public LoanStatus status() { return LoanStatus.DELINQUENT; }

    @Override public LoanState onPaymentApplied(LoanContext ctx) {
        if (ctx.isFullySettled()) return ClosedState.INSTANCE;
        if (!ctx.hasOverdueInstallment()) {
            return ctx.hasDueInstallment() ? DueState.INSTANCE : ActiveState.INSTANCE;
        }
        return ctx.isDelinquencyThresholdReached() ? this : OverdueState.INSTANCE;
    }
}