package com.javarepowizards.portfoliomanager.domain.stock;

/**
 * Read-only view of a stock’s descriptions.
 */
public interface IStockDescription {
    /** A short (~100-word) description of the stock. */
    String shortDescription();

    /** A longer (~300-word) description of the stock. */
    String longDescription();
}
