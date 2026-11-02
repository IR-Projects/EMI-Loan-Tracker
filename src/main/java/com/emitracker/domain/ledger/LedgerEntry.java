package com.emitracker.domain.ledger;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.emitracker.domain.Money;

public record LedgerEntry(
        UUID id,
        UUID loanId,
        LedgerEntryType type,
        Money amount,
        LocalDateTime timestamp,
        String idempotencyKey) {

    public LedgerEntry {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(loanId, "loanId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
    }
}