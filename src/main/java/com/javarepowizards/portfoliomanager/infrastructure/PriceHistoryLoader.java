package com.javarepowizards.portfoliomanager.infrastructure;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.StockDescription;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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

    void loadDescriptions(Path descCsv) throws IOException, CsvValidationException;
    Optional<StockDescription> getDescription(String ticker);
}
