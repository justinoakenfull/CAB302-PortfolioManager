package com.javarepowizards.portfoliomanager.domain.stock;

import java.util.Set;

/**
 * Read-only interface for accessing available stock tickers.
 */
public interface IStockRepoReadOnly {

    /**
     *
     * @return all tickers currently available in the repository
     */
    Set<String> availableTickers();
}