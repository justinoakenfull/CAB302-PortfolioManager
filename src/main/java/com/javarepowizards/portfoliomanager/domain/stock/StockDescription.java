package com.javarepowizards.portfoliomanager.domain.stock;

/**
 * Immutable implementation of IStockDescription.
 * Encapsulates a brief and detailed description for a stock.
 */
public record StockDescription(String shortDescription, String longDescription) implements IStockDescription {
    /**
     * Creates a new StockDescription with the specified text values.
     *
     * @param shortDescription a concise description of the stock
     * @param longDescription  a detailed description of the stock
     */
    public StockDescription {
    }

    /**
     * Returns the concise description of the stock.
     *
     * @return the short description string
     */
    @Override
    public String shortDescription() {
        return shortDescription;
    }

    /**
     * Returns the detailed description of the stock.
     *
     * @return the long description string
     */
    @Override
    public String longDescription() {
        return longDescription;
    }
}
