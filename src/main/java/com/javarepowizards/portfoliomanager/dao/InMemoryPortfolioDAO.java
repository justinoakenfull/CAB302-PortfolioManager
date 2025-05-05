package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.dao.StockDAO; // BAD ENCAPSULATION, remove imports later
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.StockData;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryPortfolioDAO implements IPortfolioDAO {

    private final List<PortfolioEntry> holdings = new ArrayList<>();
    private double availableBalance = 10_000;
    private StockDAO stockDAO;


    public InMemoryPortfolioDAO(){

        LocalDate date = LocalDate.of(2023, 12, 29);

        stockDAO = StockDAO.getInstance();

        StockData stock1 = stockDAO.getStockData(StockName.WES_AX, date);
        StockData stock2 = stockDAO.getStockData(StockName.TLS_AX, date);
        StockData stock3 = stockDAO.getStockData(StockName.AMC_AX, date);


        holdings.add(new PortfolioEntry(StockName.WES_AX, stock1.getClose(), 1000));
        holdings.add(new PortfolioEntry(StockName.TLS_AX, stock2.getClose(), 1000));
        holdings.add(new PortfolioEntry(StockName.AMC_AX, stock3.getClose(), 1000));

    }

    @Override public List<PortfolioEntry> getHoldings()                { return holdings; }
    @Override public double getAvailableBalance()                     { return availableBalance; }
    @Override public void addToHoldings(PortfolioEntry entry)         { holdings.add(entry); }
    @Override public double getTotalPortfolioValue()                  {
        double total = availableBalance;
        for (PortfolioEntry e : holdings) total += e.getMarketValue();
        return total;
    }
}
