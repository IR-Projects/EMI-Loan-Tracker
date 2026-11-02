package com.emitracker.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.emitracker.domain.Money;
import com.emitracker.domain.ledger.LedgerEntry;
import com.emitracker.domain.ledger.LedgerEntryType;
import com.emitracker.domain.ledger.LedgerRepository;

@Repository
public class PostgresLedgerRepository implements LedgerRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostgresLedgerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void append(LedgerEntry entry) {
        String sql = """
            INSERT INTO ledger_entries (id, loan_id, entry_type, amount_minor, currency, timestamp, idempotency_key)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                entry.id(),
                entry.loanId(),
                entry.type().name(),
                entry.amount().minorUnits(),
                entry.amount().currency().getCurrencyCode(),
                entry.timestamp(),
                entry.idempotencyKey()
        );
    }

    @Override
    public List<LedgerEntry> findByLoanId(UUID loanId) {
        String sql = "SELECT * FROM ledger_entries WHERE loan_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, new LedgerEntryRowMapper(), loanId);
    }

    private static class LedgerEntryRowMapper implements RowMapper<LedgerEntry> {
        @Override
        public LedgerEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new LedgerEntry(
                    rs.getObject("id", UUID.class),
                    rs.getObject("loan_id", UUID.class),
                    LedgerEntryType.valueOf(rs.getString("entry_type")),
                    Money.ofMinor(rs.getLong("amount_minor"), Currency.getInstance(rs.getString("currency"))),
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getString("idempotency_key")
            );
        }
    }
}