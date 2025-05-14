package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;

import java.util.List;

public class PortfolioDAO implements IPortfolioDAO{
    private List<PortfolioEntry> holdings;

    private final double availableBalance;

    public PortfolioDAO(List<PortfolioEntry> holdings, double availableBalance) {
        this.holdings = holdings;
        this.availableBalance = availableBalance;
    }

    public List<PortfolioEntry> getHoldings() {return holdings;}
    public double getAvailableBalance() {return availableBalance;}

    public void setHoldings(List<PortfolioEntry> holdings) {
        this.holdings = holdings;
    }

    public void addToHoldings(PortfolioEntry entry) {
        holdings.add(entry);
    }

    public double getTotalPortfolioValue() {
        double totalValue = availableBalance;
        for (PortfolioEntry holding : holdings) {
            totalValue += holding.getMarketValue();
        }
        return totalValue;
    }
}
