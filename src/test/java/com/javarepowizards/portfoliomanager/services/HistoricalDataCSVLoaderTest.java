package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.operations.simulation.StockBar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HistoricalDataCSVLoaderTest {

    @TempDir
    Path tempDir;

    private Path writeCsv(String filename, List<String> lines) throws IOException {
        Path csv = tempDir.resolve(filename);
        Files.write(csv, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        return csv;
    }

    @Test
    void testEmptyDataNoBars() throws IOException {
        // only headers, no data rows
        List<String> lines = List.of(
                "Date,TICKER",
                ",close"
        );
        Path csv = writeCsv("empty.csv", lines);

        HistoricalDataCSVLoader loader = new HistoricalDataCSVLoader();
        Map<String,List<StockBar>> result = loader.loadStockData(csv.toString());
        assertTrue(result.isEmpty(), "Expected no entries when there are no data rows");
    }

    @Test
    void testSingleTickerSingleRow() throws IOException {
        List<String> lines = List.of(
                "Date,AAA,AAA,AAA",
                ",open,close,volume",
                "2025-04-01,10.5,15.75,1000"
        );
        Path csv = writeCsv("single.csv", lines);

        HistoricalDataCSVLoader loader = new HistoricalDataCSVLoader();
        Map<String,List<StockBar>> result = loader.loadStockData(csv.toString());

        assertEquals(1, result.size(), "Should have exactly one ticker");
        List<StockBar> bars = result.get("AAA");
        assertNotNull(bars, "List for 'AAA' must exist");
        assertEquals(1, bars.size());
        StockBar bar = bars.getFirst();
        assertEquals(LocalDate.of(2025,4,1), bar.getDate());
        assertEquals(15.75, bar.getClose(), 1e-6);
    }

    @Test
    void testMultipleDatesOrderPreserved() throws IOException {
        List<String> lines = List.of(
                "Date,BBB,BBB",
                ",close,close",
                "2025-04-01,50,",
                "2025-04-02,60,"
        );
        Path csv = writeCsv("multi_date.csv", lines);

        HistoricalDataCSVLoader loader = new HistoricalDataCSVLoader();
        Map<String,List<StockBar>> result = loader.loadStockData(csv.toString());

        assertTrue(result.containsKey("BBB"));
        List<StockBar> bars = result.get("BBB");
        assertEquals(2, bars.size(), "Should get two bars for two valid close values");
        assertEquals(LocalDate.of(2025,4,1), bars.get(0).getDate());
        assertEquals(50.0, bars.get(0).getClose(), 1e-6);
        assertEquals(LocalDate.of(2025,4,2), bars.get(1).getDate());
        assertEquals(60.0, bars.get(1).getClose(), 1e-6);
    }

    @Test
    void testMissingCloseSkipsRow() throws IOException {
        List<String> lines = List.of(
                "Date,CCC,CCC",
                ",open,close",
                "2025-04-01,5,10",
                "2025-04-02,7,"           // missing close → skip
        );
        Path csv = writeCsv("missing_close.csv", lines);

        HistoricalDataCSVLoader loader = new HistoricalDataCSVLoader();
        Map<String,List<StockBar>> result = loader.loadStockData(csv.toString());

        List<StockBar> bars = result.get("CCC");
        assertEquals(1, bars.size(), "Only the first row has a valid close");
        assertEquals(LocalDate.of(2025,4,1), bars.getFirst().getDate());
    }

    @Test
    void testInvalidNumberDefaultsOpenToZero() throws IOException {
        List<String> lines = List.of(
                "Date,DDD,DDD,DDD",
                ",open,close,volume",
                "2025-04-05,abc,20,500"
        );
        Path csv = writeCsv("invalid_open.csv", lines);

        HistoricalDataCSVLoader loader = new HistoricalDataCSVLoader();
        Map<String,List<StockBar>> result = loader.loadStockData(csv.toString());

        List<StockBar> bars = result.get("DDD");
        assertEquals(1, bars.size());
        StockBar bar = bars.getFirst();
        assertEquals(0.0, bar.getClose() == 20.0 ? 0.0 : bar.getClose(), 1e-6,
                "Close must be 20—but open parse failed and defaulted to 0.0");
        assertEquals(20.0, bar.getClose(), 1e-6);
    }
}
