package com.emitracker.domain.loan.state;

public interface LoanContext {
    LoanState getState();
    void setState(LoanState state);

    boolean hasDueInstallment();
    boolean hasOverdueInstallment();
    boolean isDelinquencyThresholdReached();
    boolean isFullySettled();

    void auditTransition(String event, LoanState from, LoanState to);
}