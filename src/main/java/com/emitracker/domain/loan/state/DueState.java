package com.emitracker.domain.loan.state;

public final class DueState implements LoanState {
    public static final DueState INSTANCE = new DueState();
    private DueState() {}

    @Override public LoanStatus status() { return LoanStatus.DUE; }

    @Override public LoanState onGraceExpired(LoanContext ctx) {
        return ctx.hasOverdueInstallment() ? OverdueState.INSTANCE : this;
    }

    @Override public LoanState onPaymentApplied(LoanContext ctx) {
        if (ctx.isFullySettled()) return ClosedState.INSTANCE;
        if (ctx.hasOverdueInstallment()) return OverdueState.INSTANCE;
        if (ctx.hasDueInstallment()) return this;
        return ActiveState.INSTANCE;
    }
}