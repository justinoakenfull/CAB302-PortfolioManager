package com.javarepowizards.portfoliomanager.models;

public class PortfolioEntry {
    private StockName stock;
    private double purchasePrice;
    private double amountHeld;

    public PortfolioEntry(StockName stock, double purchasePrice, double amountHeld) {
        this.stock = stock;
        this.purchasePrice = purchasePrice;
        this.amountHeld = amountHeld;
    }

    public StockName getStock() { return stock; }
    public double getPurchasePrice() { return purchasePrice; }
    public int getAmountHeld() { return (int) amountHeld; }

    public double getBuyPrice() {return purchasePrice;}
    public double getQuantity() {return amountHeld;}

    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setAmountHeld(double amountHeld) { this.amountHeld = amountHeld; }

    public double getMarketValue() {
        return purchasePrice * amountHeld;
    }

    @Override
    public String toString() {
        return "StockHolding{" +
                "stockName=" + stock +
                ", purchasePrice=" + purchasePrice +
                ", amountHeld=" + amountHeld +
                ", marketValue=" + getMarketValue() +
                '}';
    }

}
