package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.operations.simulation.StockBar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Reads historical ASX trading data from a CSV file and converts it into
 * StockBar objects organized by ticker symbol.
 * Uses the first two lines of the CSV to determine column metadata,
 * then parses each subsequent row into date and value maps.
 */
public class HistoricalDataCSVLoader {

    private final String csvFilePath = "src/main/resources/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv";

    /**
     * Loads historical stock data from the specified CSV file.
     * Determines which columns correspond to open, high, low, close and volume
     * for each ticker, then builds StockBar instances for each date and ticker.
     *
     * @param csvFilePath file system path to the CSV containing ASX data
     * @return a map where each key is a ticker symbol and the value is a list
     *         of StockBar objects sorted in file order
     * @throws IOException if reading the CSV file fails
     */
    public Map<String, List<StockBar>> loadStockData(String csvFilePath) throws IOException {
        Map<String, List<StockBar>> stockData = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String[] tickerRow = br.readLine().split(",");
            String[] dataTypeRow = br.readLine().split(",");

            // Build a list of (ticker, type) for column references
            List<ColumnMeta> columns = new ArrayList<>();
            for (int i = 1; i < tickerRow.length; i++) {
                columns.add(new ColumnMeta(tickerRow[i], dataTypeRow[i], i));
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length < 2) continue;

                LocalDate date = LocalDate.parse(values[0]);
                Map<String, Double> openMap = new HashMap<>();
                Map<String, Double> highMap = new HashMap<>();
                Map<String, Double> lowMap = new HashMap<>();
                Map<String, Double> closeMap = new HashMap<>();
                Map<String, Double> volumeMap = new HashMap<>();

                for (ColumnMeta col : columns) {
                    String raw = values.length > col.index ? values[col.index] : "";
                    if (raw == null || raw.isEmpty()) continue;

                    try {
                        double val = Double.parseDouble(raw);
                        switch (col.type.toLowerCase()) {
                            case "open" -> openMap.put(col.ticker, val);
                            case "high" -> highMap.put(col.ticker, val);
                            case "low" -> lowMap.put(col.ticker, val);
                            case "close" -> closeMap.put(col.ticker, val);
                            case "volume" -> volumeMap.put(col.ticker, val);
                        }
                    } catch (NumberFormatException e) {
                        // skip invalid numbers
                    }
                }

                // Build StockBar objects
                for (String ticker : closeMap.keySet()) {
                    StockBar bar = new StockBar(
                            date,
                            ticker,
                            openMap.getOrDefault(ticker, 0.0),
                            highMap.getOrDefault(ticker, 0.0),
                            lowMap.getOrDefault(ticker, 0.0),
                            closeMap.get(ticker),
                            volumeMap.getOrDefault(ticker, 0.0)
                    );
                    stockData.computeIfAbsent(ticker, k -> new ArrayList<>()).add(bar);
                }
            }
        }

        return stockData;
    }

    private static class ColumnMeta {
        String ticker;
        String type;
        int index;

        ColumnMeta(String ticker, String type, int index) {
            this.ticker = ticker;
            this.type = type;
            this.index = index;
        }
    }
}
