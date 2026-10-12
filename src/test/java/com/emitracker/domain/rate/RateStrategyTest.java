package com.emitracker.domain.rate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class RateStrategyTest {

    // ---------- FixedRateStrategy ----------

    @Test
    void fixed_returnsSameRateForAnyDate() {
        FixedRateStrategy strategy = new FixedRateStrategy(1200);

        assertEquals(1200, strategy.rateBpsAt(LocalDate.of(2026, 1, 1)));
        assertEquals(1200, strategy.rateBpsAt(LocalDate.of(2030, 12, 31)));
    }

    @Test
    void fixed_throwsOnNullDate() {
        FixedRateStrategy strategy = new FixedRateStrategy(1200);

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> strategy.rateBpsAt(null)
        );
        assertNotNull(ex);
    }

    @Test
    void fixed_throwsOnNegativeRate() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new FixedRateStrategy(-1)
        );
        assertNotNull(ex);
    }

    @Test
    void fixed_allowsZeroRate() {
        FixedRateStrategy strategy = new FixedRateStrategy(0);
        assertEquals(0, strategy.rateBpsAt(LocalDate.of(2026, 1, 1)));
    }

    // ---------- FloatingRateStrategy ----------

    @Test
    void floating_returnsRateEffectiveOnDate() {
        Map<LocalDate, Integer> rates = new TreeMap<>();
        rates.put(LocalDate.of(2026, 1, 1), 1200);
        rates.put(LocalDate.of(2026, 6, 1), 1500);

        FloatingRateStrategy strategy = new FloatingRateStrategy(rates);

        assertEquals(1200, strategy.rateBpsAt(LocalDate.of(2026, 3, 15)));
        assertEquals(1500, strategy.rateBpsAt(LocalDate.of(2026, 7, 1)));
    }

    @Test
    void floating_returnsLatestRateAtExactBoundary() {
        Map<LocalDate, Integer> rates = new TreeMap<>();
        rates.put(LocalDate.of(2026, 1, 1), 1200);
        rates.put(LocalDate.of(2026, 6, 1), 1500);

        FloatingRateStrategy strategy = new FloatingRateStrategy(rates);

        assertEquals(1200, strategy.rateBpsAt(LocalDate.of(2026, 1, 1)));
        assertEquals(1500, strategy.rateBpsAt(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void floating_throwsOnDateBeforeEarliestRate() {
        Map<LocalDate, Integer> rates = new TreeMap<>();
        rates.put(LocalDate.of(2026, 6, 1), 1500);

        FloatingRateStrategy strategy = new FloatingRateStrategy(rates);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> strategy.rateBpsAt(LocalDate.of(2026, 1, 1))
        );
        assertNotNull(ex);
    }

    @Test
    void floating_throwsOnEmptyMap() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new FloatingRateStrategy(new HashMap<>())
        );
        assertNotNull(ex);
    }

    @Test
    void floating_throwsOnNullMap() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new FloatingRateStrategy(null)
        );
        assertNotNull(ex);
    }

    @Test
    void floating_throwsOnNegativeRate() {
        Map<LocalDate, Integer> rates = new HashMap<>();
        rates.put(LocalDate.of(2026, 1, 1), -1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new FloatingRateStrategy(rates)
        );
        assertNotNull(ex);
    }

    @Test
    void floating_throwsOnNullDate() {
        Map<LocalDate, Integer> rates = new HashMap<>();
        rates.put(LocalDate.of(2026, 1, 1), 1200);

        FloatingRateStrategy strategy = new FloatingRateStrategy(rates);

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> strategy.rateBpsAt(null)
        );
        assertNotNull(ex);
    }

    @Test
    void floating_doesNotMutateSourceMap() {
        Map<LocalDate, Integer> rates = new HashMap<>();
        rates.put(LocalDate.of(2026, 1, 1), 1200);

        FloatingRateStrategy strategy = new FloatingRateStrategy(rates);

        // Mutate the source map after construction.
        rates.put(LocalDate.of(2026, 6, 1), 1500);

        // If the defensive copy works, the strategy only knows about 2026-01-01,
        // so querying 2026-07-01 falls back to 1200 — NOT 1500.
        assertEquals(1200, strategy.rateBpsAt(LocalDate.of(2026, 7, 1)));
    }
}