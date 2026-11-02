CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL
);

CREATE INDEX idx_ledger_loan_id ON ledger_entries(loan_id);