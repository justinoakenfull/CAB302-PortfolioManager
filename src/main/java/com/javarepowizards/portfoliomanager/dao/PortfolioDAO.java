package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;

import java.util.List;

public class PortfolioDAO implements IPortfolioDAO {

    // List of PortfolioEntry objects representing the user's current holdings
    private List<PortfolioEntry> holdings;

    // Available cash balance (outside of holdings)
    private final double availableBalance;

    // Constructor taking an initial holdings list and starting cash balance
    public PortfolioDAO(List<PortfolioEntry> holdings, double availableBalance) {
        this.holdings = holdings;
        this.availableBalance = availableBalance;
    }

    // Returns the list of current holdings
    public List<PortfolioEntry> getHoldings() {
        return holdings;
    }

    // Returns the available cash balance
    public double getAvailableBalance() {
        return availableBalance;
    }

    // Replaces the current holdings list with a new one
    public void setHoldings(List<PortfolioEntry> holdings) {
        this.holdings = holdings;
    }

    // Adds a new entry to the holdings list
    public void addToHoldings(PortfolioEntry entry) {
        holdings.add(entry);
    }

    // Calculates the total portfolio value (cash + sum of all holding market values)
    public double getTotalPortfolioValue() {
        double totalValue = availableBalance; // start with cash balance
        for (PortfolioEntry holding : holdings) {
            totalValue += holding.getMarketValue(); // add each holding's market value
        }
        return totalValue;
    }
}
