package com.javarepowizards.portfoliomanager.domain.stock;

import com.javarepowizards.portfoliomanager.domain.price.PriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IStock {
    /** The ticker symbol, e.g. "AAPL" or "BHP". */
    String getTicker();

    /** Human-readable name, e.g. "Apple Inc." */
    String getCompanyName();

    /** The latest available PriceRecord (today, or last trading day). */
    PriceRecord getCurrentRecord();

    /**
     * Historical data service – delegates to PriceHistory.
     */
    PriceHistory getHistory();

    /**
     * Convenience: price for a specific day, if available.
     */
    default Optional<PriceRecord> getRecord(LocalDate date) {
        return getHistory().getRecord(date);
    }

    /**
     * Convenience: all data in a date range.
     */
    default List<PriceRecord> getRecords(LocalDate start, LocalDate end) {
        return getHistory().getRecords(start, end);
    }

    /**
     * Convenience: last 365 days of data.
     */
    default List<PriceRecord> getLastYear() {
        return getHistory().getLastYear();
    }
}
