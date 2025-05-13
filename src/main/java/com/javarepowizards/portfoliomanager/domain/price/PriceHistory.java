package com.javarepowizards.portfoliomanager.domain.price;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceHistory {
    /**
     * Get the record for exactly this date, if it exists.
     */
    Optional<PriceRecord> getRecord(LocalDate date);

    /**
     * All records between (inclusive) start and end, sorted by date.
     */
    List<PriceRecord> getRecords(LocalDate start, LocalDate end);

    /**
     * Convenience: last 365 days from “today.”
     */
    default List<PriceRecord> getLastYear() {
        LocalDate today = LocalDate.now();
        return getRecords(today.minusYears(1), today);
    }
}
