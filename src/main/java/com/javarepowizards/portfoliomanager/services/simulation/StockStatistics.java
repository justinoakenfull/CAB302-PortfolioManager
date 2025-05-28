package com.javarepowizards.portfoliomanager.services.simulation;

import com.javarepowizards.portfoliomanager.models.StockData;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StockStatistics {

    private final double averageDailyReturn;

    private final double volatility;

    private final double momentum;


    public StockStatistics(List<StockData> stockDataList){
        // ensure the list is sorted by date in ascending order.

        Collections.sort(stockDataList, Comparator.comparing(StockData::getDate));

        int size = stockDataList.size();
        if (size < 2){
            throw new IllegalArgumentException("Not enough data to calculate statistics");
        }

        //calculate daily returns using simple return calculation
        // daily return = current close - previous close / previous close

        double sumReturns = 0.0;
        double sumSquaredReturns = 0.0;
        double[] returns = new double[size - 1];

        for (int i = 1; i < size; i++){
            double previousClose = stockDataList.get(i - 1).getClose();
            double currentClose = stockDataList.get(i).getClose();

            double dailyReturn = (currentClose - previousClose) / previousClose;
            returns[i - 1] = dailyReturn;
            sumReturns += dailyReturn;
            sumSquaredReturns += dailyReturn * dailyReturn;
        }

        int n = returns.length;
        this.averageDailyReturn = sumReturns / n;

        // calculate volatility (standard deviation) using the variance formula.
        double variance = (sumSquaredReturns / n) - (averageDailyReturn * averageDailyReturn);
        this.volatility = Math.sqrt(variance);

        // baseline momentum: average of most recent momentum period returns.
        int momentumPeriod = 10;
        if(n < momentumPeriod){
            momentum = averageDailyReturn;
        } else{
            double sumMomentum = 0.0;
            for (int i = n - momentumPeriod; i < n; i++){
                sumMomentum += returns[i];
            }
            this.momentum = sumMomentum / momentumPeriod;
        }
    }

    public double getAverageDailyReturn(){
        return averageDailyReturn;
    }

    public double getVolatility(){
        return volatility;
    }

    public double getMomentum(){
        return momentum;
    }
}
