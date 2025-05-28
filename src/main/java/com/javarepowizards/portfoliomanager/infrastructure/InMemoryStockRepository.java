package com.javarepowizards.portfoliomanager.infrastructure;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.Stock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.domain.stock.StockDescription;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the StockRepository interface.
 * Uses CSV-based loader to read price history and descriptions,
 * and caches created IStock instances for reuse.
 */
public class InMemoryStockRepository implements StockRepository {
    private final PriceHistoryLoader loader;
    private final Map<String,IStock> cache = new ConcurrentHashMap<>();

    /**
     * Constructs the repository with paths to the ASX price CSV and a descriptions CSV.
     * The descriptions CSV must contain rows of ticker, short description, and long description.
     *
     * @param priceCsvPath path to the ASX price data CSV file
     * @param descCsvPath path to the CSV containing stock descriptions
     * @throws IOException if reading either CSV fails
     * @throws CsvValidationException if the CSV content is invalid
     */
    public InMemoryStockRepository(Path priceCsvPath,
                                   Path descCsvPath)
            throws IOException, CsvValidationException
    {
        this.loader = new OpenCsvAsxLoader(priceCsvPath);
        // loadDescriptions reads Ticker,ShortDescription,LongDescription
        loader.loadDescriptions(descCsvPath);
    }

    /**
     * Returns the set of all ticker symbols available in the price CSV.
     *
     * @return a set of ticker strings
     */
    @Override
    public Set<String> availableTickers() {
        return loader.availableTickers();
    }

    /**
     * Retrieves or creates an IStock instance for the given ticker.
     * Price history is loaded on first request and stored in cache.
     * If no history exists for the ticker, a stub record with zero values
     * for the current date is used. Descriptions default to blank if absent.
     *
     * @param ticker the stock ticker symbol
     * @return an IStock instance containing price history and descriptions
     */
    @Override
    public IStock getByTicker(String ticker) {
        return cache.computeIfAbsent(ticker, t -> {
            List<PriceRecord> history;
            try {
                history = loader.loadHistory(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (history.isEmpty()) {
                // optional: create stub
                history = List.of(new PriceRecord(LocalDate.now(), 0,0,0,0,0));
            }
            // look up display name from enum
            StockName sn = StockName.fromString(t);

            // pull the description (or default to blanks)
            var desc = loader.getDescription(t)
                    .orElse(new StockDescription("", ""));
            return new Stock(
                    t,
                    sn.getDisplayName(),
                    history,
                    desc.shortDescription(),
                    desc.longDescription()
            );
        });
    }

    /**
     * Returns a list of IStock instances for all available tickers.
     * Stocks are retrieved in the order of the available tickers set iteration.
     *
     * @return unmodifiable list of all loaded IStock instances
     */
    @Override
    public List<IStock> getAll() {
        return availableTickers().stream()
                .map(this::getByTicker)
                .toList();
    }
}
