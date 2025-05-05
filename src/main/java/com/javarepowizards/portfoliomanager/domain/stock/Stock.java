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

    /**
     * @param allRecords must include the latest record as its last element.
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
    }

    @Override public String      getTicker()        { return ticker;        }
    @Override public String      getCompanyName()   { return companyName;   }
    @Override public PriceRecord getCurrentRecord() { return currentRecord; }
    @Override public PriceHistory getHistory()      { return history;       }
}
