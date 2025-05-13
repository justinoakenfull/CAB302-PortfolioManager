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
import java.util.stream.Collectors;

public class InMemoryStockRepository implements StockRepository {
    private final PriceHistoryLoader loader;
    private final Map<String,IStock> cache = new ConcurrentHashMap<>();
    /**
     * @param csvPath Path to your ASX price CSV.
     */
    public InMemoryStockRepository(Path csvPath) throws IOException, CsvValidationException {
        this.loader = new OpenCsvAsxLoader(csvPath);
    }

    /**
     * @param priceCsvPath Path to your ASX price CSV.
     * @param descCsvPath  Path to your descriptions.csv.
     */
    public InMemoryStockRepository(Path priceCsvPath,
                                   Path descCsvPath)
            throws IOException, CsvValidationException
    {
        this.loader = new OpenCsvAsxLoader(priceCsvPath);
        // loadDescriptions reads Ticker,ShortDescription,LongDescription
        loader.loadDescriptions(descCsvPath);
    }

    @Override
    public Set<String> availableTickers() {
        return loader.availableTickers();
    }

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
                    desc.getShortDescription(),
                    desc.getLongDescription()
            );
        });
    }

    @Override
    public List<IStock> getAll() {
        return availableTickers().stream()
                .map(this::getByTicker)
                .collect(Collectors.toUnmodifiableList());
    }
}
