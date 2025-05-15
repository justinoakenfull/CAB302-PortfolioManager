package com.javarepowizards.portfoliomanager.domain.stock;
/**
 * Immutable implementation of IStockDescription.
 * Encapsulates a brief and detailed description for a stock.
 */
public class StockDescription implements IStockDescription {
    private final String shortDescription;
    private final String longDescription;

    /**
     * Creates a new StockDescription with the specified text values.
     *
     * @param shortDescription a concise description of the stock
     * @param longDescription a detailed description of the stock
     */
    public StockDescription(String shortDescription, String longDescription) {
        this.shortDescription = shortDescription;
        this.longDescription  = longDescription;
    }

    /**
     * Returns the concise description of the stock.
     *
     * @return the short description string
     */
    @Override
    public String getShortDescription() { return shortDescription; }

    /**
     * Returns the detailed description of the stock.
     *
     * @return the long description string
     */
    @Override
    public String getLongDescription()  { return longDescription;  }
}
