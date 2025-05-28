package com.javarepowizards.portfoliomanager.dao.stock;

import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * API for loading and querying historical stock data from CSV.
 */
public interface IStockDAO {

    /**
     * Loads stock data from the CSV file at the given path.
     *
     * @param filePath path to the CSV file
     * @throws IOException if reading the file fails
     */
    void loadCSV(String filePath) throws IOException;

    /**
     * Returns all loaded StockData entries for the given stock symbol.
     *
     * @param stockName the stock symbol enum
     * @return list of StockData for that symbol, or an empty list if none
     */
    List<StockData> getStockData(StockName stockName);

    /**
     * Retrieves a single StockData entry for a given stock symbol on a specified date.
     *
     * @param stockName the stock symbol enum
     * @param date      the specific date to look up
     * @return the matching StockData, or null if none found
     */
    StockData getStockData(StockName stockName, LocalDate date);
}
