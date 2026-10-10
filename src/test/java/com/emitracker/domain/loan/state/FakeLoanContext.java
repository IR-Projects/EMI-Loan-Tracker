package com.emitracker.domain.loan.state;

public class FakeLoanContext implements LoanContext {
    private LoanState state = ActiveState.INSTANCE;
    private boolean due, overdue, delinquent, settled;

    @Override public LoanState getState() { return state; }
    
    // This single method satisfies the interface AND acts as a test setup helper
    @Override public void setState(LoanState state) { this.state = state; }
    
    @Override public boolean hasDueInstallment() { return due; }
    @Override public boolean hasOverdueInstallment() { return overdue; }
    @Override public boolean isDelinquencyThresholdReached() { return delinquent; }
    @Override public boolean isFullySettled() { return settled; }
    @Override public void auditTransition(String event, LoanState from, LoanState to) { /* no-op for tests */ }

    // Helpers for test setup (NOTE: setState is NOT here anymore)
    public void setDue(boolean due) { this.due = due; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
    public void setDelinquent(boolean delinquent) { this.delinquent = delinquent; }
    public void setSettled(boolean settled) { this.settled = settled; }
}