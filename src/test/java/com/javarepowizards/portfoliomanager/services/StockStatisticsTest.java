package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.models.StockData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


public class StockStatisticsTest {
    /** Helper to make StockData with a given date and closing price. */
    private StockData makeData(LocalDate date, double close) {
        StockData sd = new StockData(date);
        sd.setClose(close);
        return sd;
    }

    @Test
    void volatility_isZero_whenAllClosesSame() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        // Three days, all close = 100 => returns [0,0] => volatility = 0
        List<StockData> data = new ArrayList<>(List.of(
                makeData(start,       100),
                makeData(start.plusDays(1), 100),
                makeData(start.plusDays(2), 100)
        ));
        StockStatistics stats = new StockStatistics(data);
        assertEquals(0.0, stats.getVolatility(), 1e-12);
        assertEquals(0.0, stats.getAverageDailyReturn(), 1e-12);
        assertEquals(0.0, stats.getMomentum(), 1e-12);
    }

    @Test
    void averageAndVolatility_forUniformReturns() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        // Two days: 100 -> 110 -> 121 gives returns [0.1, 0.1]
        List<StockData> data = new ArrayList<>(List.of(
                makeData(start,       100),
                makeData(start.plusDays(1), 110),
                makeData(start.plusDays(2), 121)
        ));
        StockStatistics stats = new StockStatistics(data);

        // Average daily return = (0.1 + 0.1)/2 = 0.1
        assertEquals(0.1, stats.getAverageDailyReturn(), 1e-12);
        // Variance = E[r^2] - mu^2 = ((0.1^2+0.1^2)/2) - 0.1^2 = 0 => volatility = 0
        assertEquals(0.0, stats.getVolatility(), 1e-12);
        // Not enough for 10‐day momentum => falls back to average = 0.1
        assertEquals(0.1, stats.getMomentum(), 1e-12);
    }

    @Test
    void momentum_usesLastTenReturns_whenEnoughData() {
        LocalDate start = LocalDate.of(2023, 1, 1);

        // Build exactly 11 days of data so there are 10 returns.
        // We choose returns of 0.1, 0.2, ..., 1.0.
        double[] desiredReturns = IntStream.rangeClosed(1, 10)
                .mapToDouble(i -> i / 10.0)
                .toArray();

        List<StockData> data = new ArrayList<>();
        // Day 0
        double close = 100.0;
        data.add(makeData(start, close));

        // For each desired return r, next close = prevClose * (1 + r)
        for (int i = 0; i < desiredReturns.length; i++) {
            close = close * (1 + desiredReturns[i]);
            data.add(makeData(start.plusDays(i + 1), close));
        }

        StockStatistics stats = new StockStatistics(data);

        // Expected momentum = average of the ten returns (0.1 through 1.0) = (0.1+...+1.0)/10 = 0.55
        double sum = Arrays.stream(desiredReturns).sum();
        double expectedMomentum = sum / desiredReturns.length;
        assertEquals(expectedMomentum, stats.getMomentum(), 1e-9);

        // Average daily return should also equal the same average
        assertEquals(expectedMomentum, stats.getAverageDailyReturn(), 1e-9);
    }

}
