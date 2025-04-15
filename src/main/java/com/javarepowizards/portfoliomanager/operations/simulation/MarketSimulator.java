package com.javarepowizards.portfoliomanager.operations.simulation;

import java.time.LocalDate;
import java.util.*;

public class MarketSimulator {

    private final Random random = new Random();

    public List<Double> simulateStock(StockProfile profile, double initialPrice, int days) {
        List<Double> prices = new ArrayList<>();
        double price = initialPrice;
        double previousReturn = 0.0;

        for (int i = 0; i < days; i++) {
            double randomShock = random.nextGaussian() * profile.getBaseVolatility();
            double momentumEffect = profile.getMomentumWeight() * previousReturn;
            double dailyReturn = profile.getAvgDailyReturn() + randomShock + momentumEffect;

            // Cap return (max daily movement ±10%)
            dailyReturn = Math.max(-0.1, Math.min(0.1, dailyReturn));

            // Apply dynamic upper/lower bounds (±2.5×volatility as a base)
            double upperBound = 1 + 2.5 * profile.getBaseVolatility();
            double lowerBound = 1 - 2.5 * profile.getBaseVolatility();

            double nextPrice = price * (1 + dailyReturn);
            nextPrice = Math.min(price * upperBound, Math.max(price * lowerBound, nextPrice));

            prices.add(nextPrice);
            previousReturn = dailyReturn;
            price = nextPrice;
        }

        return prices;
    }

    public void runSimulation(Map<String, List<StockBar>> stockData) {
        int simulationDays = 5;

        for (String ticker : stockData.keySet()) {
            List<StockBar> bars = stockData.get(ticker);
            if (bars.isEmpty()) continue;

            StockBar lastBar = bars.get(bars.size() - 1);
            double initialPrice = lastBar.getClose();
            LocalDate startDate = lastBar.getDate();

            StockProfile profile = new StockProfile("BHP", 0.001, 0.02, 0.3);
            List<Double> simulatedPrices = simulateStock(profile, initialPrice, simulationDays);

            List<StockBar> simulatedBars = new ArrayList<>();
            for (int i = 0; i < simulatedPrices.size(); i++) {
                LocalDate date = startDate.plusDays(i + 1);
                double close = simulatedPrices.get(i);

                // Mocking open/high/low/volume for now
                double open = close * (1 + random.nextDouble() * 0.01 - 0.005);
                double high = Math.max(open, close) * (1 + random.nextDouble() * 0.01);
                double low = Math.min(open, close) * (1 - random.nextDouble() * 0.01);
                double volume = 100000 + random.nextInt(100000);

                StockBar bar = new StockBar(date, ticker, open, high, low, close, volume);
                simulatedBars.add(bar);
            }

            // Output or use simulatedBars as needed
            System.out.println("Simulated bars for " + ticker + ":");
            for (StockBar bar : simulatedBars) {
                System.out.println(bar);
            }
        }
    }
}
