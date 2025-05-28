package com.javarepowizards.portfoliomanager.domain;

import com.javarepowizards.portfoliomanager.domain.price.InMemoryPriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryPriceHistoryTest {

    // three distinct records, sorted by date
    private final PriceRecord r1 = new PriceRecord(
            LocalDate.of(2025, 1, 1),
            10.0,   // open
            15.0,   // high
            5.0,    // low
            12.0,   // close
            100L    // volume
    );
    private final PriceRecord r2 = new PriceRecord(
            LocalDate.of(2025, 2, 1),
            20.0,
            25.0,
            15.0,
            22.0,
            200L
    );
    private final PriceRecord r3 = new PriceRecord(
            LocalDate.of(2025, 3, 1),
            30.0,
            35.0,
            25.0,
            32.0,
            300L
    );

    @Test
    void constructor_throwsOnUnsortedList() {
        List<PriceRecord> bad = List.of(r2, r1, r3);
        assertThrows(IllegalArgumentException.class,
                () -> new InMemoryPriceHistory(bad),
                "Expected constructor to throw when records are not sorted by date");
    }

    @Test
    void constructor_defensiveCopy() {
        List<PriceRecord> original = new ArrayList<>(List.of(r1, r2));
        InMemoryPriceHistory hist = new InMemoryPriceHistory(original);

        // Mutate the original list and ensure our history is unaffected
        original.clear();
        assertTrue(
                hist.getRecord(LocalDate.of(2025, 1, 1)).isPresent(),
                "Expected InMemoryPriceHistory to have made a defensive copy of the input list"
        );
    }

    @Test
    void getRecord_foundAndNotFound() {
        InMemoryPriceHistory hist = new InMemoryPriceHistory(List.of(r1, r2));

        // present cases
        assertTrue(hist.getRecord(r1.date()).isPresent());
        assertEquals(r1, hist.getRecord(r1.date()).get());
        assertEquals(r2, hist.getRecord(r2.date()).orElseThrow());

        // missing date
        assertTrue(
                hist.getRecord(LocalDate.of(2024, 12, 31)).isEmpty(),
                "Expected getRecord to return empty when no record matches the date"
        );
    }

    @Test
    void getRecords_inclusiveRange() {
        InMemoryPriceHistory hist = new InMemoryPriceHistory(List.of(r1, r2, r3));

        // full range includes all three
        List<PriceRecord> all = hist.getRecords(r1.date(), r3.date());
        assertEquals(List.of(r1, r2, r3), all);

        // sub-range from r2 to r3
        List<PriceRecord> sub = hist.getRecords(r2.date(), r3.date());
        assertEquals(List.of(r2, r3), sub);

        // range with no hits
        List<PriceRecord> none = hist.getRecords(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
        assertTrue(none.isEmpty(), "Expected empty list when no records fall in the given range");
    }
}
