package com.javarepowizards.portfoliomanager.dao;

import java.time.LocalDate;
import java.util.*;
import com.javarepowizards.portfoliomanager.operations.simulation.StockBar;

public class MockDataGenerator {

    public static Map<String, List<StockBar>> generateMockStockData() {
        Map<String, List<StockBar>> stockData = new HashMap<>();

        // Example: REA.AX with 3 days of data
        List<StockBar> reaBars = List.of(
                new StockBar(LocalDate.of(2020, 1, 2), "REA.AX", 98.75, 99.80, 98.66, 99.41, 84196),
                new StockBar(LocalDate.of(2020, 1, 3), "REA.AX", 101.48, 102.29, 100.63, 100.63, 79290),
                new StockBar(LocalDate.of(2020, 1, 4), "REA.AX", 102.00, 103.00, 101.00, 102.50, 80000)
        );

        List<StockBar> cohBars = List.of(
                new StockBar(LocalDate.of(2020, 1, 2), "COH.AX", 212.47, 214.27, 211.82, 212.12, 79256),
                new StockBar(LocalDate.of(2020, 1, 3), "COH.AX", 214.59, 216.31, 212.08, 212.46, 82410),
                new StockBar(LocalDate.of(2020, 1, 4), "COH.AX", 215.00, 217.00, 213.00, 216.00, 85000)
        );

        stockData.put("REA.AX", reaBars);
        stockData.put("COH.AX", cohBars);

        return stockData;
    }

}
