package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.StockData;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of the portfolio data access object.
 * Stores portfolio entries and available balance in memory.
 * Initializes with a default set of holdings as of a fixed date.
 */
@Repository
public class InMemoryPortfolioDAO implements IPortfolioDAO {

    private final List<PortfolioEntry> holdings = new ArrayList<>();
    private double availableBalance = 10_000;
    private StockDAO stockDAO;

    /**
     * Constructs the in-memory portfolio DAO and populates initial holdings.
     * Uses a fixed date to fetch sample stock data for initial entries.
     */
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

    /**
     * Returns the list of current portfolio entries.
     *
     * @return a list of portfolio entries held in memory
     */
    @Override public List<PortfolioEntry> getHoldings()                { return holdings; }

    /**
     * Returns the available cash balance for the portfolio.
     *
     * @return the available balance
     */
    @Override public double getAvailableBalance()                     { return availableBalance; }

    /**
     * Adds a new entry to the portfolio holdings.
     *
     * @param entry the portfolio entry to add
     */
    @Override public void addToHoldings(PortfolioEntry entry)         { holdings.add(entry); }

    /**
     * Computes the total value of the portfolio.
     * Includes the available cash balance plus the market value of all holdings.
     *
     * @return the total portfolio value
     */
    @Override public double getTotalPortfolioValue()                  {
        double total = availableBalance;
        for (PortfolioEntry e : holdings) total += e.getMarketValue();
        return total;
    }

    @Override
    public void upsertHolding(int userId, StockName stock, int quantity, double totalValue) {
        // This assumes you store a list of PortfolioEntry like PortfolioDAO does.
        holdings.add(new PortfolioEntry(stock, totalValue / quantity, quantity));
    }

    @Override
    public List<PortfolioEntry> getHoldingsForUser(int userId) {
        return holdings;
    }

}
