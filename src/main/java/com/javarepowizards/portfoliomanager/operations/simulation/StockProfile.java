package com.javarepowizards.portfoliomanager.operations.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class StockProfile {

    private final String ticker;
    private final double baseVolatility;
    private final double avgDailyReturn;
    private final double momentumWeight;

    public StockProfile(String ticker, double baseVolatility, double avgDailyReturn, double momentumWeight) {
        this.ticker = ticker;
        this.baseVolatility = baseVolatility;
        this.avgDailyReturn = avgDailyReturn;
        this.momentumWeight = momentumWeight;
    }

    public String getTicker() {
        return ticker;
    }

    public double getBaseVolatility() {
        return baseVolatility;
    }

    public double getAvgDailyReturn() {
        return avgDailyReturn;
    }

    public double getMomentumWeight() {
        return momentumWeight;
    }

    @Override
    public String toString() {
        return "StockProfile{" +
                "ticker='" + ticker + '\'' +
                ", baseVolatility=" + baseVolatility +
                ", avgDailyReturn=" + avgDailyReturn +
                ", momentumWeight=" + momentumWeight +
                '}';
    }



}




