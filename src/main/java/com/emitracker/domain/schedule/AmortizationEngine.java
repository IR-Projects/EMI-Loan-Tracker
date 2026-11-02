package com.emitracker.domain.schedule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.emitracker.domain.Loan;
import com.emitracker.domain.Money;

public class AmortizationEngine {

    public Schedule generate(Loan loan) {
        int n = loan.getTenureMonths();
        Money principal = loan.getPrincipal();
        LocalDate startDate = loan.getStartDate();
        
        List<Installment> installments = new ArrayList<>();
        long remainingPrincipalMinor = principal.minorUnits();
        
        // 1. Aylık sabit ödeme tutarını (EMI) hesapla
        Money fixedEmi = calculateEmi(loan);

                for (int i = 1; i <= n; i++) {
            // Vade tarihini hesapla (LocalDate 31. günü otomatik olarak bir sonraki ayın son gününe ayarlar)
            LocalDate dueDate = startDate.plusMonths(i);
            
            // 2. O ayki faiz tutarını hesapla
            Money interestForMonth = calculateInterest(remainingPrincipalMinor, loan.getInterestRateBps(), principal.currency());

            long principalForMonthMinor;

            if (i == n) {
                // SON TAKSİT: Yuvarlama artığını emer. Kalan tüm anapara buraya yazılır.
                principalForMonthMinor = remainingPrincipalMinor;
            } else {
                // Normal taksitler
                principalForMonthMinor = fixedEmi.minorUnits() - interestForMonth.minorUnits();
            }

            // Kalan anaparayı güncelle
            remainingPrincipalMinor -= principalForMonthMinor;

            // B'nin modeline göre Installment oluştur
            installments.add(new Installment(
                    i,
                    dueDate,
                    Money.ofMinor(principalForMonthMinor, principal.currency()),
                    interestForMonth,
                    Money.ofMinor(remainingPrincipalMinor, principal.currency())
            ));
        }

        return new Schedule(loan.getId(), installments);
    }

    private Money calculateEmi(Loan loan) {
        long pMinor = loan.getPrincipal().minorUnits();
        int n = loan.getTenureMonths();
        int bps = loan.getInterestRateBps();

        if (bps == 0) {
            // Faizsiz kredi: Anapara / Vade
            return Money.ofMinor(pMinor / n, loan.getPrincipal().currency());
        }

        // EMI Formülü: P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal p = BigDecimal.valueOf(pMinor);
        
        // Aylık faiz oranı: Yıllık bps / 10000 / 12
        BigDecimal monthlyRate = BigDecimal.valueOf(bps)
                .divide(BigDecimal.valueOf(120000), 10, RoundingMode.HALF_UP);
                
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal compound = onePlusR.pow(n);

        BigDecimal emi = p.multiply(monthlyRate).multiply(compound)
                .divide(compound.subtract(BigDecimal.ONE), 0, RoundingMode.HALF_UP);

        return Money.ofMinor(emi.longValueExact(), loan.getPrincipal().currency());
    }

    private Money calculateInterest(long remainingPrincipalMinor, int bps, java.util.Currency currency) {
        BigDecimal balance = BigDecimal.valueOf(remainingPrincipalMinor);
        
        // Aylık faiz oranı
        BigDecimal monthlyRate = BigDecimal.valueOf(bps)
                .divide(BigDecimal.valueOf(120000), 10, RoundingMode.HALF_UP);
                
        BigDecimal interest = balance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
        
        return Money.ofMinor(interest.longValueExact(), currency);
    }
}