package com.emitracker.infrastructure.persistence;

import com.emitracker.domain.Money;
import com.emitracker.domain.ledger.LedgerEntry;
import com.emitracker.domain.ledger.LedgerEntryType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostgresLedgerRepositoryTest {

    @Autowired
    private PostgresLedgerRepository repository;

    @Test
    void appendAndFindByLoanId_worksCorrectly() {
        // 1. Rastgele bir kredi ID'si oluştur
        UUID loanId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        
        // 2. Sahte bir Ledger kaydı oluştur
        LedgerEntry entry = new LedgerEntry(
                entryId,
                loanId,
                LedgerEntryType.DISBURSEMENT,
                Money.ofMinor(100_000, Currency.getInstance("TRY")),
                LocalDateTime.now(),
                "test-idempotency-key-" + UUID.randomUUID() // Benzersiz olmalı
        );

        // 3. Veritabanına kaydet
        repository.append(entry);

        // 4. Geri oku ve doğrula
        List<LedgerEntry> foundEntries = repository.findByLoanId(loanId);

        assertEquals(1, foundEntries.size(), "Veritabanında 1 kayıt bulunmalı.");
        assertEquals(entryId, foundEntries.get(0).id(), "Kayıt ID'si eşleşmeli.");
        assertEquals(100_000, foundEntries.get(0).amount().minorUnits(), "Tutar eşleşmeli.");
    }
}