package com.javarepowizards.portfoliomanager.models;

import java.time.LocalDate;

/**
 * The StockData class models the financial data for a single stock on a particular date.
 * It contains key information such as price, volume, and other trading metrics.
 */
public class StockData {
    // The date for which this stock data is applicable.
    // This field is declared final since the date associated with this record is immutable once created.
    private final LocalDate date;

    private final String symbol;

    // The current price of the stock. #TODO Decide which open/close to use for price.
    private Double price;

    // The opening price of the stock on the recorded day.
    private Double open;

    // The highest price reached by the stock on the recorded day.
    private Double high;

    // The lowest price reached by the stock on the recorded day.
    private Double low;

    // The closing price of the stock on the recorded day.
    private Double close;

    // The trading volume (number of shares) for the stock on the recorded day.
    private Long volume;

    // The adjusted close price of the stock.
    // This value might differ from the raw closing price if corporate actions such as dividends or splits have occurred.
    // This field is optional and may be null if not provided in the data source.
    private Double adjClose;

    /**
     * Constructs a new StockData instance associated with the given date.
     * @param date the LocalDate representing the day this data corresponds to.
     */
    public StockData(LocalDate date, String symbol) {
        this.date = date;  // Assign the provided date to the immutable 'date' field.
        this.symbol = symbol;
    }

    /**
     * Returns the date for which this stock data applies.
     * @return the LocalDate of this data record.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the stock's general price (can be used as a current or closing price).
     * @return the price of the stock as a Double.
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Sets the price of the stock.
     * @param price the new price as a Double.
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Returns the opening price of the stock.
     * @return the opening price as a Double.
     */
    public Double getOpen() {
        return open;
    }

    /**
     * Sets the opening price of the stock.
     * @param open the opening price as a Double.
     */
    public void setOpen(Double open) {
        this.open = open;
    }

    /**
     * Returns the highest price of the stock during the day.
     * @return the highest price as a Double.
     */
    public Double getHigh() {
        return high;
    }

    /**
     * Sets the highest price of the stock for the day.
     * @param high the highest price as a Double.
     */
    public void setHigh(Double high) {
        this.high = high;
    }

    /**
     * Returns the lowest price of the stock during the day.
     * @return the lowest price as a Double.
     */
    public Double getLow() {
        return low;
    }

    /**
     * Sets the lowest price of the stock for the day.
     * @param low the lowest price as a Double.
     */
    public void setLow(Double low) {
        this.low = low;
    }

    /**
     * Returns the closing price of the stock.
     * @return the closing price as a Double.
     */
    public Double getClose() {
        return close;
    }

    /**
     * Sets the closing price of the stock.
     * @param close the closing price as a Double.
     */
    public void setClose(Double close) {
        this.close = close;
    }

    /**
     * Returns the trading volume (the number of shares traded) for the stock.
     * @return the volume as a Long.
     */
    public Long getVolume() {
        return volume;
    }

    /**
     * Sets the trading volume for the stock.
     * @param volume the trading volume as a Long.
     */
    public void setVolume(Long volume) {
        this.volume = volume;
    }

    /**
     * Returns the adjusted closing price of the stock.
     * This may account for events like dividends or stock splits.
     * @return the adjusted closing price as a Double (or null if not set).
     */
    public Double getAdjClose() {
        return adjClose;
    }

    /**
     * Sets the adjusted closing price for the stock.
     * @param adjClose the adjusted closing price as a Double.
     */
    public void setAdjClose(Double adjClose) {
        this.adjClose = adjClose;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns a String representation of this StockData object.
     * This is useful for debugging and logging purposes.
     *
     * @return a String containing key stock data fields.
     */
    @Override
    public String toString() {
        return "StockData{" +
                "date=" + date +
                ", price=" + price +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", adjClose=" + adjClose +
                '}';
    }
}
