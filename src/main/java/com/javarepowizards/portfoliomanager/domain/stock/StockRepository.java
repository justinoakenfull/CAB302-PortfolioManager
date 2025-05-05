package com.javarepowizards.portfoliomanager.domain.stock;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface StockRepository {
    /** All tickers currently available. */
    Set<String> availableTickers();

    /**
     * Returns the unique Stock for this ticker (creates it once, thereafter reuses it).
     * Throws if no data.
     */
    IStock getByTicker(String ticker) throws IOException;

    /**
     * All stocks loaded (one instance per ticker).
     */
    List<IStock> getAll() throws IOException;
}
