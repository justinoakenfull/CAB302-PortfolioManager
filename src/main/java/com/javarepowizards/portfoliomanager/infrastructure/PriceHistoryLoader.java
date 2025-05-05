package com.javarepowizards.portfoliomanager.infrastructure;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Abstraction for anything that can supply historical PriceRecords by ticker.
 */
public interface PriceHistoryLoader {
    /** All tickers available in the CSV. */
    Set<String> availableTickers();

    /**
     * List of PriceRecords for this ticker.
     */
    List<PriceRecord> loadHistory(String ticker) throws IOException;
}
