package com.javarepowizards.portfoliomanager.domain.price;

import java.time.LocalDate;

/**
 * Immutable record of daily price and volume data for a stock.
 * Stores open, high, low, close values and trading volume for a specific date.
 */
public final class PriceRecord {
    private final LocalDate date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    /**
     * Creates a new PriceRecord with the specified values.
     *
     * @param date the trading date for this record
     * @param open the opening price on the given date
     * @param high the highest price reached on the given date
     * @param low the lowest price reached on the given date
     * @param close the closing price on the given date
     * @param volume the total trading volume on the given date
     */
    public PriceRecord(LocalDate date,
                       double open,
                       double high,
                       double low,
                       double close,
                       long volume) {
        this.date   = date;
        this.open   = open;
        this.high   = high;
        this.low    = low;
        this.close  = close;
        this.volume = volume;
    }

    /**
     * Returns the date associated with this record.
     *
     * @return the LocalDate of this price record
     */
    public LocalDate getDate()     { return date;   }

    /**
     * Returns the opening price for the date.
     *
     * @return the opening price value
     */
    public double    getOpen()     { return open;   }

    /**
     * Returns the highest price reached on the date.
     *
     * @return the high price value
     */
    public double    getHigh()     { return high;   }

    /**
     * Returns the lowest price reached on the date.
     *
     * @return the low price value
     */
    public double    getLow()      { return low;    }

    /**
     * Returns the closing price for the date.
     *
     * @return the closing price value
     */
    public double    getClose()    { return close;  }

    /**
     * Returns the trading volume for the date.
     *
     * @return the volume as a long integer
     */
    public long      getVolume()   { return volume; }
}
