package com.javarepowizards.portfoliomanager.services.utility;

import java.time.LocalDate;

public class StockBar {

    private LocalDate date;
    private String ticker;
    private double open, high, low, close, volume;

    public StockBar(LocalDate date, String ticker, double open,
                    double high, double low, double close, double volume) {
        this.date = date;
        this.ticker = ticker;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;

    }

    public LocalDate getDate() {
        return date;
    }

    public double getClose() {
        return close;
    }

}
