package com.emitracker.domain.schedule;

import com.emitracker.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AmortizationEngineTest {

    private final AmortizationEngine engine = new AmortizationEngine();
    private final Currency TRY = Currency.getInstance("TRY");

    private Loan createLoan(long principalMinor, int tenureMonths, int bps, LocalDate startDate) {
        return new Loan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test Loan",
                null,
                Money.ofMinor(principalMinor, TRY),
                bps,
                tenureMonths,
                15, // dueDayOfMonth
                5,  // gracePeriodDays
                startDate,
                "Test Bank",
                RateType.FIXED,
                LoanStatus.ACTIVE
        );
    }

    @Test
    void generate_zeroInterest_SplitsEqually() {
        // 1200 TL (120.000 kuruş), 12 ay, faizsiz (%0)
        Loan loan = createLoan(120_000, 12, 0, LocalDate.of(2026, 1, 1));
        
        Schedule schedule = engine.generate(loan);

        assertEquals(12, schedule.size());
        // Her ay anapara 10.000 kuruş (100 TL) olmalı
        assertEquals(10_000, schedule.getInstallments().get(0).principal().minorUnits());
        // Faiz sıfır olmalı
        assertEquals(0, schedule.getInstallments().get(0).interest().minorUnits());
        // Kalan bakiye 110.000 olmalı
        assertEquals(110_000, schedule.getInstallments().get(0).balanceAfter().minorUnits());
        
        // Kural: Tüm anaparaların toplamı, kredinin anaparasına EŞİT olmalı
        assertEquals(loan.getPrincipal().minorUnits(), schedule.totalPrincipal().minorUnits());
    }

    @Test
    void generate_withInterest_LastInstallmentAbsorbsRemainder() {
        // 1000 TL (100.000 kuruş), 12 ay, Yıllık %12 (1200 bps) -> Aylık %1
        Loan loan = createLoan(100_000, 12, 1200, LocalDate.of(2026, 1, 1));
        
        Schedule schedule = engine.generate(loan);

        assertEquals(12, schedule.size());
        
        // Aylık sabit ödeme (EMI) kabaca 8884 kuruş (88.84 TL) çıkar.
        // Yuvarlama farkı son taksite ekleneceği için ilk 11 taksit aynı olmalı.
        long firstInstallmentTotal = schedule.getInstallments().get(0).principal().minorUnits() 
                                   + schedule.getInstallments().get(0).interest().minorUnits();
        
        long secondInstallmentTotal = schedule.getInstallments().get(1).principal().minorUnits() 
                                    + schedule.getInstallments().get(1).interest().minorUnits();
        
        assertEquals(firstInstallmentTotal, secondInstallmentTotal);

        // EN KRİTİK TEST: Yuvarlama artığı son taksite eklendiği için,
        // TÜM TAKSİTLERİN ANAPARA TOPLAMI, KREDİ ANPARASINA TAM EŞİT OLMALI.
        assertEquals(loan.getPrincipal().minorUnits(), schedule.totalPrincipal().minorUnits());
        
        // Son taksitten sonra kalan bakiye 0 olmalı
        Installment last = schedule.getInstallments().get(11);
        assertEquals(0, last.balanceAfter().minorUnits());
    }

    @Test
    void generate_handlesMonthEndsCorrectly() {
        // 31 Ocak'ta başlayan kredi
        Loan loan = createLoan(100_000, 3, 1200, LocalDate.of(2026, 1, 31));
        
        Schedule schedule = engine.generate(loan);

        // 31 Ocak + 1 Ay = 28 Şubat (2026 artık yıl değil)
        assertEquals(LocalDate.of(2026, 2, 28), schedule.getInstallments().get(0).dueDate());
        // 31 Ocak + 2 Ay = 31 Mart
        assertEquals(LocalDate.of(2026, 3, 31), schedule.getInstallments().get(1).dueDate());
        // 31 Ocak + 3 Ay = 30 Nisan
        assertEquals(LocalDate.of(2026, 4, 30), schedule.getInstallments().get(2).dueDate());
    }
}