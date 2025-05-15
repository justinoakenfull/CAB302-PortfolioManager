package com.javarepowizards.portfoliomanager.domain;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PriceRecordTest {

    @Test
    void constructorAndGettersReturnTheSameValues() {
        LocalDate date = LocalDate.of(2025, 5, 14);
        double open  = 100.0;
        double high  = 110.0;
        double low   =  90.0;
        double close = 105.0;
        long   volume= 1_000L;

        PriceRecord rec = new PriceRecord(date, open, high, low, close, volume);

        assertEquals(date,   rec.getDate());
        assertEquals(open,   rec.getOpen());
        assertEquals(high,   rec.getHigh());
        assertEquals(low,    rec.getLow());
        assertEquals(close,  rec.getClose());
        assertEquals(volume, rec.getVolume());
    }
}
