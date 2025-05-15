package com.javarepowizards.portfoliomanager.domain;

import com.javarepowizards.portfoliomanager.domain.price.PriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IStockDefaultMethodsTest {

    /** A tiny stub of PriceHistory that always returns the same record/list. */
    private static class StubHistory implements PriceHistory {
        private final PriceRecord record;
        private final List<PriceRecord> records;

        StubHistory(PriceRecord record, List<PriceRecord> records) {
            this.record = record;
            this.records = records;
        }

        @Override
        public Optional<PriceRecord> getRecord(LocalDate date) {
            return Optional.of(record);
        }

        @Override
        public List<PriceRecord> getRecords(LocalDate start, LocalDate end) {
            return records;
        }

        @Override
        public List<PriceRecord> getLastYear() {
            return records;
        }
    }

    // sample data
    private final PriceRecord sample =
            new PriceRecord(LocalDate.of(2025, 5, 14),
                    100.0, 110.0,  90.0, 105.0,
                    1_000L);
    private final List<PriceRecord> sampleList = List.of(sample);

    // an anonymous IStock whose getHistory() returns our stub
    private final IStock stock = new IStock() {
        @Override public String getTicker()              { return "FOO"; }
        @Override public String getCompanyName()         { return "Foo Corp"; }
        @Override public PriceRecord getCurrentRecord()  { return sample; }
        @Override public PriceHistory getHistory()       { return new StubHistory(sample, sampleList); }
        @Override public String getShortDescription()    { return "short"; }
        @Override public String getLongDescription()     { return "long"; }
    };

    @Test
    void getRecord_delegatesToHistory() {
        Optional<PriceRecord> result = stock.getRecord(LocalDate.of(2025,1,1));
        assertTrue(result.isPresent());
        assertEquals(sample, result.get());
    }

    @Test
    void getRecords_delegatesToHistory() {
        List<PriceRecord> result =
                stock.getRecords(LocalDate.of(2025,1,1), LocalDate.of(2025,12,31));
        assertEquals(sampleList, result);
    }

    @Test
    void getLastYear_delegatesToHistory() {
        List<PriceRecord> result = stock.getLastYear();
        assertEquals(sampleList, result);
    }
}
