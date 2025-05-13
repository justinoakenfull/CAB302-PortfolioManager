package com.javarepowizards.portfoliomanager.domain.stock;

public class StockDescription implements IStockDescription {
    private final String shortDescription;
    private final String longDescription;

    public StockDescription(String shortDescription, String longDescription) {
        this.shortDescription = shortDescription;
        this.longDescription  = longDescription;
    }

    @Override
    public String getShortDescription() { return shortDescription; }
    @Override
    public String getLongDescription()  { return longDescription;  }
}
