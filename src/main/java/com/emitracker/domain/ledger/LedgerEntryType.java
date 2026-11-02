package com.emitracker.domain.ledger;

public enum LedgerEntryType {
    DISBURSEMENT,      // Kredi verilmesi (Anapara)
    INTEREST_ACCRUAL,  // Faiz tahakkuku
    FEE_ACCRUAL,       // Ceza/Gecikme ücreti tahakkuku
    PAYMENT,           // Kullanıcının ödemesi
    REVERSAL           // Hatalı kaydı tersine çevirme
}