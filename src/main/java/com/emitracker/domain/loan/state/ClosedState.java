package com.emitracker.domain.loan.state;

public final class ClosedState implements LoanState {
    public static final ClosedState INSTANCE = new ClosedState();
    private ClosedState() {}

    @Override public LoanStatus status() { return LoanStatus.CLOSED; }

    @Override public LoanState onLoanSettled(LoanContext ctx) { return this; }
}