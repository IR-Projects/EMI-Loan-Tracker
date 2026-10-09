package com.emitracker.domain;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;


public class MoneyTest {

    @Test
    void plus_addsMinorUnits() {
        Money a = Money.ofMinor(100, Currency.getInstance("TRY"));
        Money b = Money.ofMinor(50, Currency.getInstance("TRY"));

        Money result = a.plus(b);

        assertEquals(150, result.minorUnits());
    }

    @Test
    void minus_subtractsMinorUnits() {
        Money a = Money.ofMinor(200, Currency.getInstance("TRY"));
        Money b = Money.ofMinor(50, Currency.getInstance("TRY"));

        Money result = a.minus(b);

        assertEquals(150, result.minorUnits());
    }

    @Test
    void plus_throwsOnDifferentCurrency() {
        Money tryMoney = Money.ofMinor(100, Currency.getInstance("TRY"));
        Money usdMoney = Money.ofMinor(100, Currency.getInstance("USD"));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> tryMoney.plus(usdMoney)
        );
        assertNotNull(ex);
    }

    @Test
    void plus_throwsOnOverflow() {
        Money max = Money.ofMinor(Long.MAX_VALUE, Currency.getInstance("TRY"));
        Money one = Money.ofMinor(1, Currency.getInstance("TRY"));

        ArithmeticException ex = assertThrows(
            ArithmeticException.class,
            () -> max.plus(one)
        );
        assertNotNull(ex);
    }

    @Test
    void multiplyByBps_exactDivision() {
        Money m = Money.ofMinor(100, Currency.getInstance("TRY"));

        Money result = m.multiplyByBps(100);

        assertEquals(1, result.minorUnits());
    }

    @Test
    void multiplyByBps_exactHalfRoundsUp() {
        Money m = Money.ofMinor(1, Currency.getInstance("TRY"));

        Money result = m.multiplyByBps(5000);

        assertEquals(1, result.minorUnits());
    }

    @Test
    void multiplyByBps_belowHalfRoundsDown() {
        Money m = Money.ofMinor(1, Currency.getInstance("TRY"));

        Money result = m.multiplyByBps(4999);

        assertEquals(0, result.minorUnits());
    }

    @Test
void equals_sameValueAndCurrency() {
    Money a = Money.ofMinor(100, Currency.getInstance("TRY"));
    Money b = Money.ofMinor(100, Currency.getInstance("TRY"));

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
}

@Test
void equals_differentCurrencyNotEqual() {
    Money tryMoney = Money.ofMinor(100, Currency.getInstance("TRY"));
    Money usdMoney = Money.ofMinor(100, Currency.getInstance("USD"));

    assertNotEquals(tryMoney, usdMoney);
}

}