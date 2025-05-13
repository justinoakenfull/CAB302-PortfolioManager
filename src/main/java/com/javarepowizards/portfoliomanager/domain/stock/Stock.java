package com.javarepowizards.portfoliomanager.domain.stock;

import com.javarepowizards.portfoliomanager.domain.price.PriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.InMemoryPriceHistory;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;

import java.util.List;

public class Stock implements IStock {
    private final String       ticker;
    private final String       companyName;
    private final PriceRecord  currentRecord;
    private final PriceHistory history;
    private final String ShortDescription;
    private final String LongDescription;

    /**
     * @param allRecords must include the latest record as its last element.
     *            //XXX: This is provided for backward compatibility. New
     *                   stocks will include a short and long description.
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
     * @param allRecords must include the latest record as its last element.
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

    @Override public String      getTicker()        { return ticker;        }
    @Override public String      getCompanyName()   { return companyName;   }
    @Override public PriceRecord getCurrentRecord() { return currentRecord; }
    @Override public PriceHistory getHistory()      { return history;       }
    @Override public String getShortDescription() { return ShortDescription; }
    @Override public String getLongDescription() { return LongDescription; }
}
