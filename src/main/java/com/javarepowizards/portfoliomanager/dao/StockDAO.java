// The DAO package, indicating that this class is responsible for data access operations.
package com.javarepowizards.portfoliomanager.dao;

// Import the StockName enum which defines the allowed stock symbols.
import com.javarepowizards.portfoliomanager.MainApplication;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.StockData;

// Import necessary classes for I/O, date handling, and collections.
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The StockDAO class is responsible for reading stock data from a CSV file
 * and making it available via in-memory lookup methods.
 * It uses an EnumMap keyed by StockName to store a list of StockData objects for each stock.
 * The CSV is assumed to have a specific header format and data layout.
 */
public class StockDAO {

    private static final String DEFAULT_CSV_PATH = "/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv";

    private static class Holder {
        private static final StockDAO INSTANCE = new StockDAO();
    }
    public static StockDAO getInstance() {
        return Holder.INSTANCE;
    }

    private StockDAO() {
        try {
            // Attempt to locate the CSV on the classpath
            URL url = MainApplication.class.getResource(DEFAULT_CSV_PATH);
            // Null‐check to avoid NullPointerException if the resource is missing (probably not needed)
            if (url == null) {
                throw new RuntimeException("Could not find resource on classpath: " + DEFAULT_CSV_PATH);
            }
            // Load the CSV
            loadCSV(url.getFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stock data from " + DEFAULT_CSV_PATH, e);
        }
    }

    // stockDataMap uses an EnumMap to hold a list of StockData entries for each StockName.
    // The use of an EnumMap guarantees type-safety and efficient key lookups.
    private final Map<StockName, List<StockData>> stockDataMap = new EnumMap<>(StockName.class);

    /**
     * Loads the CSV file given its file path.
     * The CSV file is expected to contain three header lines:
     *    1. The first header row lists the stock symbols (repeated in column groups).
     *    2. The second header row lists the field names (e.g., Price, Open, High, etc.).
     *    3. The third header row is used for other meta-information (e.g., a "Date" label) and is ignored.
     *
     * The method then processes each subsequent line as data.
     *
     * @param filePath the String path to the CSV file.
     * @throws IOException if the file cannot be read.
     */
    public void loadCSV(String filePath) throws IOException {
        // Open the file using a BufferedReader wrapped around a FileReader.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read the first header row that contains the stock symbols.
            String headerSymbolsLine = br.readLine();
            // Read the second header row that contains the field names.
            String headerFieldsLine = br.readLine();
            // Read the third header row that may simply be used to denote the "Date" column.
            String headerIgnoreLine = br.readLine();

            // Check if any of the header lines are missing; if so, throw an exception to signal the misformatted CSV.
            if (headerSymbolsLine == null || headerFieldsLine == null || headerIgnoreLine == null) {
                throw new IllegalArgumentException("CSV file does not contain required header rows.");
            }

            // Split the header lines into arrays for individual columns.
            String[] symbolHeaders = headerSymbolsLine.split(",");
            String[] fieldHeaders = headerFieldsLine.split(",");

            // Trim any extra whitespace from each header value.
            for (int i = 0; i < symbolHeaders.length; i++) {
                symbolHeaders[i] = symbolHeaders[i].trim();
            }
            for (int i = 0; i < fieldHeaders.length; i++) {
                fieldHeaders[i] = fieldHeaders[i].trim();
            }

            // Read each subsequent data line until the end of the file.
            String line;
            while ((line = br.readLine()) != null) {
                // Skip empty lines to avoid processing blank rows.
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Split the current line by commas into tokens. The tokens array holds
                // both the date (first token) and the subsequent field values.
                String[] tokens = line.split(",");
                // If the row doesn't have enough tokens, then skip it.
                if (tokens.length < 2) {
                    continue;
                }
                // The first token represents the date; parse it into a LocalDate.
                LocalDate date = LocalDate.parse(tokens[0].trim());

                // Use a temporary EnumMap to store StockData objects for each stock encountered
                // in this row. This allows us to combine multiple fields into a single StockData object.
                Map<StockName, StockData> rowStockData = new EnumMap<>(StockName.class);

                // Loop over each token starting from index 1 (skipping the date).
                // Ensure we do not go past the limits defined by both header arrays.
                for (int i = 1; i < tokens.length && i < symbolHeaders.length && i < fieldHeaders.length; i++) {
                    // Get the corresponding stock symbol from the first header row.
                    String symbolStr = symbolHeaders[i];
                    StockName stockName;
                    try {
                        // Convert the symbol string into a StockName enum constant.
                        stockName = StockName.fromString(symbolStr);
                    } catch (IllegalArgumentException e) {
                        // If conversion fails (symbol not recognized), skip this token.
                        System.err.println("Error parsing value \"" + symbolStr);
                        continue;
                    }
                    // Get the field name (e.g., "Price", "Open", etc.) from the second header row.
                    String field = fieldHeaders[i];
                    // Get and trim the value from the current token.
                    String valueStr = tokens[i].trim();
                    // If the token is empty, skip it.
                    if (valueStr.isEmpty()) {
                        continue;
                    }
                    // Retrieve (or create) a StockData object for this stock for the current date.
                    StockData sd = rowStockData.computeIfAbsent(stockName, k -> new StockData(date));
                    try {
                        // Depending on the field name, parse the value as Double or Long and set it in the StockData object.
                        switch (field) {
                            case "Price":
                                sd.setPrice(Double.parseDouble(valueStr));
                                break;
                            case "Open":
                                sd.setOpen(Double.parseDouble(valueStr));
                                break;
                            case "High":
                                sd.setHigh(Double.parseDouble(valueStr));
                                break;
                            case "Low":
                                sd.setLow(Double.parseDouble(valueStr));
                                break;
                            case "Close":
                                sd.setClose(Double.parseDouble(valueStr));
                                break;
                            case "Volume":
                                sd.setVolume(Long.parseLong(valueStr));
                                break;
                            case "Adj Close":
                                sd.setAdjClose(Double.parseDouble(valueStr));
                                break;
                            default:
                                // If the field is unknown, simply skip it.
                                break;
                        }
                    } catch (NumberFormatException ex) {
                        // If parsing fails, log the error along with the problematic value and field.
                        System.err.println("Error parsing value \"" + valueStr + "\" for field " + field);
                    }
                }
                // After processing all tokens in the row, merge the data from rowStockData into the main stockDataMap.
                // For each stock in the row, add its StockData record to the corresponding list in the map.
                for (Map.Entry<StockName, StockData> entry : rowStockData.entrySet()) {
                    // If no list exists for the given stock, create a new one; then add the new StockData record.
                    stockDataMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
                }
            }
        }
    }

    /**
     * Retrieves the list of stock data entries for the specified stock symbol.
     *
     * @param stockName the stock symbol enum (e.g., StockName.WES_AX)
     * @return a list of StockData objects corresponding to that stock,
     *         or an empty list if no data is available.
     */
    public List<StockData> getStockData(StockName stockName) {
        // Use getOrDefault to return an empty list if no data is found.
        return stockDataMap.getOrDefault(stockName, Collections.emptyList());
    }

    /**
     * Retrieves a single StockData entry for a given stock symbol on a specified date.
     * This method iterates over the list of StockData objects for the stock until it finds one
     * that matches the provided date.
     *
     * @param stockName the stock symbol enum.
     * @param date the specific LocalDate for which data is requested.
     * @return the StockData entry if found, or null if no matching entry exists.
     */
    public StockData getStockData(StockName stockName, LocalDate date) {
        // Loop over the list of StockData objects for the given stock.
        for (StockData sd : getStockData(stockName)) {
            // Check if the record's date matches the requested date.
            if (sd.getDate().equals(date)) {
                return sd;
            }
        }
        // If no record matches the date, return null.
        return null;
    }
}
