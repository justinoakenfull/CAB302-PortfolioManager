package com.javarepowizards.portfoliomanager.operations.simulation;

import java.util.*;

public class StockStatisticsCalculator {

    public Map<String, StockProfile> computeStockProfiles(Map<String, List<StockBar>> stockData) {
        Map<String, StockProfile> profiles = new HashMap<>();

        for (String ticker : stockData.keySet()) {
            List<StockBar> bars = stockData.get(ticker);
            if (bars.size() < 2) continue;

            // Sort bars by date
            bars.sort(Comparator.comparing(StockBar::getDate));

            List<Double> dailyReturns = new ArrayList<>();
            for (int i = 1; i < bars.size(); i++) {
                double prevClose = bars.get(i - 1).getClose();
                double currClose = bars.get(i).getClose();
                if (prevClose == 0) continue;

                double dailyReturn = (currClose - prevClose) / prevClose;
                dailyReturns.add(dailyReturn);
            }

            double avgReturn = dailyReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double volatility = calculateStdDev(dailyReturns, avgReturn);
            double momentumWeight = Math.random() * 0.5 + 0.5; // Optional/random for now

            StockProfile profile = new StockProfile(ticker, volatility, avgReturn, momentumWeight);
            profiles.put(ticker, profile);
        }

        return profiles;
    }

    private double calculateStdDev(List<Double> values, double mean) {
        double sumSq = 0;
        for (double val : values) {
            sumSq += Math.pow(val - mean, 2);
        }
        return Math.sqrt(sumSq / values.size());
    }
}
