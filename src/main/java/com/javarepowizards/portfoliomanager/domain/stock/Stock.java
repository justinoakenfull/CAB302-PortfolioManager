package com.javarepowizards.portfoliomanager.domain.stock;

import com.javarepowizards.portfoliomanager.domain.price.PriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.InMemoryPriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;

import java.util.List;

/**
 * Concrete implementation of IStock representing a stock with its
 * price history and optional descriptions.
 * Instances are immutable after construction.
 */
public class Stock implements IStock {
    private final String       ticker;
    private final String       companyName;
    private final PriceRecord  currentRecord;
    private final PriceHistory history;
    private final String ShortDescription;
    private final String LongDescription;

    /**
     * Constructs a Stock using only price records.
     * This constructor is provided for backward compatibility.
     * Short and long descriptions default to empty strings.
     *
     * @param ticker the stock ticker symbol, not null or empty
     * @param companyName the full name of the company, not null
     * @param allRecords a list of PriceRecord objects sorted by date,
     *                   with the latest record as its last element
     * @throws IllegalArgumentException if allRecords is empty
     */
    public Stock(String ticker,
                 String companyName,
                 List<PriceRecord> allRecords) {
        if (allRecords.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one record");
        }
        this.ticker        = ticker;
        this.companyName   = companyName;
        // last element is "current"
        this.currentRecord = allRecords.getLast();
        // wrap history (including current) in an immutable history
        this.history       = new InMemoryPriceHistory(allRecords);
        this.ShortDescription = "";
        this.LongDescription = "";
    }

    /**
     * Constructs a Stock with price records and textual descriptions.
     *
     * @param ticker the stock ticker symbol, not null or empty
     * @param companyName the full name of the company, not null
     * @param allRecords a list of PriceRecord objects sorted by date,
     *                   with the latest record as its last element
     * @param shortDescription a brief description of the stock, not null
     * @param longDescription a detailed description of the stock, not null
     * @throws IllegalArgumentException if allRecords is empty
     */
    public Stock(String ticker,
                 String companyName,
                 List<PriceRecord> allRecords,
                 String shortDescription,
                 String longDescription) {
        if (allRecords.isEmpty())
            throw new IllegalArgumentException("Must supply at least one record");

        this.ticker           = ticker;
        this.companyName      = companyName;
        this.currentRecord    = allRecords.getLast();
        this.history          = new InMemoryPriceHistory(allRecords);
        this.ShortDescription = shortDescription;
        this.LongDescription  = longDescription;
    }

    /**
     * Returns the stock ticker symbol.
     *
     * @return the ticker string
     */
    @Override public String      getTicker()        { return ticker;        }

    /**
     * Returns the company’s full name.
     *
     * @return the companyName string
     */
    @Override public String      getCompanyName()   { return companyName;   }

    /**
     * Returns the most recent price record.
     *
     * @return the current PriceRecord
     */
    @Override public PriceRecord getCurrentRecord() { return currentRecord; }

    /**
     * Returns the full price history.
     *
     * @return a PriceHistory containing all records, including current
     */
    @Override public PriceHistory getHistory()      { return history;       }


    /**
     * Returns the brief description of the stock.
     *
     * @return the shortDescription string
     */
    @Override public String getShortDescription() { return ShortDescription; }

    /**
     * Returns the detailed description of the stock.
     *
     * @return the longDescription string
     */
    @Override public String getLongDescription() { return LongDescription; }
}
