package com.javarepowizards.portfoliomanager.domain.price;

import java.time.LocalDate;

/**
 * Immutable record of daily price and volume data for a stock.
 * Stores open, high, low, close values and trading volume for a specific date.
 */
public record PriceRecord(LocalDate date, double open, double high, double low, double close, long volume) {
    /**
     * Creates a new PriceRecord with the specified values.
     *
     * @param date   the trading date for this record
     * @param open   the opening price on the given date
     * @param high   the highest price reached on the given date
     * @param low    the lowest price reached on the given date
     * @param close  the closing price on the given date
     * @param volume the total trading volume on the given date
     */
    public PriceRecord {
    }

    /**
     * Returns the date associated with this record.
     *
     * @return the LocalDate of this price record
     */
    @Override
    public LocalDate date() {
        return date;
    }

    /**
     * Returns the opening price for the date.
     *
     * @return the opening price value
     */
    @Override
    public double open() {
        return open;
    }

    /**
     * Returns the highest price reached on the date.
     *
     * @return the high price value
     */
    @Override
    public double high() {
        return high;
    }

    /**
     * Returns the lowest price reached on the date.
     *
     * @return the low price value
     */
    @Override
    public double low() {
        return low;
    }

    /**
     * Returns the closing price for the date.
     *
     * @return the closing price value
     */
    @Override
    public double close() {
        return close;
    }

    /**
     * Returns the trading volume for the date.
     *
     * @return the volume as a long integer
     */
    @Override
    public long volume() {
        return volume;
    }
}
