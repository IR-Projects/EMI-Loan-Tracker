package com.emitracker.domain.ledger;

import java.util.List;
import java.util.UUID;

public interface LedgerRepository {
    void append(LedgerEntry entry);
    List<LedgerEntry> findByLoanId(UUID loanId);
}