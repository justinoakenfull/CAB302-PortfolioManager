package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.models.StockData;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


public class StockDataFilter {


    /** returns a filtered list of StockData containing only the entries from the last year.
     * @param stockData List the list of StockData to filter
     * @param mostRecentDate the most recent date
     * @return a list of stockData that have dates within our one year before most recent date
     */


    public List<StockData> getDataFromLastYear(List<StockData> stockData, LocalDate mostRecentDate){
        // compute lower bound date (one year before the most recent date)
        LocalDate lowerLimit = mostRecentDate.minusYears(1);

        // Use java streams to filter databetween lower limit and mostRecent date
        return stockData.stream()
                .filter(data -> !data.getDate(). isBefore(lowerLimit) && !data.getDate().isAfter(mostRecentDate))
                .collect(Collectors.toList());
    }
}
