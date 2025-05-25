package com.javarepowizards.portfoliomanager.domain.stock;

import com.javarepowizards.portfoliomanager.domain.IStockRepoReadOnly;

import java.io.IOException;
import java.util.List;

/**
 * Repository for stock data.
 * Provides methods to query available tickers and load stock details.
 */

public interface StockRepository extends IStockRepoReadOnly {

    /**
     *
     * @param ticker the stock symbol
     * @return a single IStock instance for the given ticker
     * @throws IOException if data cannot be loaded
     */
    IStock getByTicker(String ticker) throws IOException;

    /**
     *
     * @return list of all stocks loaded
     * @throws IOException if data cannot be loaded
     */
    List<IStock> getAll() throws IOException;
}
