package com.javarepowizards.portfoliomanager.objects;


import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.javarepowizards.portfoliomanager.domain.stock.Stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



public class StockTest {
    static Stock stock;
    static PriceRecord record;
    @BeforeAll
    static void init() {
        List<PriceRecord> records = new ArrayList<>();

        // Create a sample PriceRecord
        record = new PriceRecord(
                LocalDate.of(2024, 4, 29), // today's date
                150.0,   // open price
                155.0,   // high price
                149.0,   // low price
                154.0,   // close price
                5_000_000L // volume
        );

        records.add(record);

        stock = new Stock("AAPL", "Apple Inc.", records);
    }

    @Test
    void testStockInitialization() {
        assertNotNull(stock);
        assertEquals("AAPL", stock.getTicker());
        assertEquals("Apple Inc.", stock.getCompanyName());
        assertEquals(record, stock.getCurrentRecord());
    }
}