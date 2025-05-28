package com.javarepowizards.portfoliomanager.infrastructure;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.StockDescription;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;


/**
 * Loader that reads ASX price history and optional descriptions from CSV files.
 * Stores price records and descriptions in memory for fast lookup.
 */
public class OpenCsvAsxLoader implements PriceHistoryLoader {
    private final Map<String, List<PriceRecord>> data = new HashMap<>();
    private final Map<String, StockDescription> descriptions = new HashMap<>();

    /**
     * Constructs a loader and reads price history from the given CSV file.
     * Expects the CSV to have two header lines for tickers and fields, then data rows.
     * After loading, price records for each ticker are sorted by date.
     *
     * @param csvPath path to the ASX price CSV file
     * @throws IOException if an I/O error occurs while reading the file
     * @throws CsvValidationException if the CSV format is invalid
     */
    public OpenCsvAsxLoader(Path csvPath) throws IOException, CsvValidationException {
        try (Reader r = Files.newBufferedReader(csvPath);
             CSVReader reader = new CSVReader(r)) {

            // 1) read the two true header lines
            String[] tickers = reader.readNext();    // e.g. ["Ticker","REA.AX","REA.AX",…]
            String[] fields  = reader.readNext();    // e.g. ["Price","Open","High",…]
            reader.readNext();                       // skip the blank “Date,…” row

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length==0 || row[0].isBlank()) continue;
                LocalDate date = LocalDate.parse(row[0].trim());

                // accumulate raw field values per ticker
                Map<String,Map<String,String>> rowMap = new LinkedHashMap<>();
                for (int i = 1; i < tickers.length && i < row.length; i++) {
                    String t = tickers[i].trim();
                    if (t.isEmpty()) continue;
                    String f = fields[i].trim();
                    String v = row[i].trim();
                    if (v.isEmpty()) continue;

                    rowMap
                            .computeIfAbsent(t, __-> new HashMap<>())
                            .put(f, v);
                }

                // for each ticker, pull out the five fields we care about
                for (var entry : rowMap.entrySet()) {
                    String t = entry.getKey();
                    Map<String,String> vals = entry.getValue();
                    // skip incomplete rows
                    if (!vals.containsKey("Open")  ||
                            !vals.containsKey("High")  ||
                            !vals.containsKey("Low")   ||
                            !vals.containsKey("Close") ||
                            !vals.containsKey("Volume")) {
                        continue;
                    }

                    double open   = Double.parseDouble(vals.get("Open"));
                    double high   = Double.parseDouble(vals.get("High"));
                    double low    = Double.parseDouble(vals.get("Low"));
                    double close  = Double.parseDouble(vals.get("Close"));
                    long   volume = Long.parseLong(vals.get("Volume"));

                    PriceRecord rec = new PriceRecord(date, open, high, low, close, volume);
                    data.computeIfAbsent(t, __-> new ArrayList<>()).add(rec);
                }
            }
        }

        // finally sort each ticker’s history
        data.values()
                .forEach(list -> list.sort(Comparator.comparing(PriceRecord::date)));
    }

    /**
     * Reads stock descriptions from the specified CSV file.
     * The CSV must have at least three columns: ticker, short description, long description.
     *
     * @param descCsv path to the descriptions CSV file
     * @throws IOException if an I/O error occurs while reading the file
     * @throws CsvValidationException if the CSV format is invalid or header is missing
     */
    public void loadDescriptions(Path descCsv) throws IOException, CsvValidationException {
        try (Reader r = Files.newBufferedReader(descCsv);
             CSVReader reader = new CSVReader(r)) {

            String[] header = reader.readNext();
            if (header == null || header.length < 3) {
                throw new CsvValidationException("Expected header with at least 3 columns");
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 3 || row[0].isBlank()) continue;

                String ticker   = row[0].trim().replace("\"", "");
                String shortDesc= row[1].trim().replace("\"", "");
                String longDesc = row[2].trim().replace("\"", "");

                descriptions.put(ticker, new StockDescription(shortDesc, longDesc));
            }
        }
    }

    /**
     * Returns the description for a given ticker, if one was loaded.
     *
     * @param ticker the stock ticker symbol
     * @return an Optional containing the StockDescription or empty if not present
     */
    public Optional<StockDescription> getDescription(String ticker) {
        return Optional.ofNullable(descriptions.get(ticker));
    }


    /**
     * Returns the set of all tickers for which price history has been loaded.
     *
     * @return an unmodifiable set of ticker strings
     */
    @Override
    public Set<String> availableTickers() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * Returns the full price history for the specified ticker.
     * If no data exists for the ticker, returns an empty list.
     *
     * @param ticker the stock ticker symbol
     * @return an unmodifiable list of PriceRecord objects
     */
    @Override
    public List<PriceRecord> loadHistory(String ticker) {
        return Collections.unmodifiableList(data.getOrDefault(ticker, List.of()));
    }
}
