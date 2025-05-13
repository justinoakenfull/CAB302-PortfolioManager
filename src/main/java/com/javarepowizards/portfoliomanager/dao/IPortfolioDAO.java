package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import java.util.List;

public interface IPortfolioDAO {

    List<PortfolioEntry> getHoldings();
    double getAvailableBalance();
    void addToHoldings(PortfolioEntry entry);
    double getTotalPortfolioValue();
}
