package com.emitracker.domain.loan.state;

public interface LoanState {
    LoanStatus status();

    default LoanState onDueDateReached(LoanContext ctx) { return this; }
    default LoanState onGraceExpired(LoanContext ctx) { return this; }
    default LoanState onDelinquencyThresholdReached(LoanContext ctx) { return this; }
    default LoanState onPaymentApplied(LoanContext ctx) { return this; }
    default LoanState onLoanSettled(LoanContext ctx) { return ClosedState.INSTANCE; }
}
