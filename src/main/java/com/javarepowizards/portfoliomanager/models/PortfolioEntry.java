package com.javarepowizards.portfoliomanager.models;

/**
 * Represents an entry in a user’s portfolio, including the stock identifier,
 * the price at which it was purchased, and the quantity held.
 */
public class PortfolioEntry {
    private final StockName stock;
    private double purchasePrice;
    private double amountHeld;

    /**
     * Constructs a new PortfolioEntry.
     *
     * @param stock the stock identifier and metadata
     * @param purchasePrice the price paid per share
     * @param amountHeld the number of shares held
     */
    public PortfolioEntry(StockName stock, double purchasePrice, double amountHeld) {
        this.stock = stock;
        this.purchasePrice = purchasePrice;
        this.amountHeld = amountHeld;
    }


    /**
     * Returns the stock metadata for this entry.
     *
     * @return the StockName instance
     */
    public StockName getStock() { return stock; }

    /**
     * Returns the purchase price per share.
     *
     * @return the purchase price
     */
    public double getPurchasePrice() { return purchasePrice; }

    /**
     * Returns the number of shares held, cast to an integer.
     *
     * @return the number of shares held
     */
    public int getAmountHeld() { return (int) amountHeld; }

    /**
     * Returns the purchase price per share.
     * Alias for getPurchasePrice.
     *
     * @return the purchase price
     */
    public double getBuyPrice() {return purchasePrice;}

    /**
     * Returns the quantity of shares held.
     * Alias for the raw amountHeld value.
     *
     * @return the quantity of shares
     */
    public double getQuantity() {return amountHeld;}

    /**
     * Updates the purchase price per share.
     *
     * @param purchasePrice the new purchase price
     */
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    /**
     * Updates the number of shares held.
     *
     * @param amountHeld the new number of shares
     */
    public void setAmountHeld(double amountHeld) { this.amountHeld = amountHeld; }

    /**
     * Calculates the total market value of this holding.
     *
     * @return purchasePrice multiplied by amountHeld
     */
    public double getMarketValue() {
        return purchasePrice * amountHeld;
    }

    /**
     * Returns a string representation of this portfolio entry,
     * including stock, purchase price, amount held, and market value.
     *
     * @return a string describing the entry
     */
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
