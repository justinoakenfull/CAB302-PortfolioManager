package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PortfolioInitializer {

    public static PortfolioDAO createDummyPortfolio(StockDAO stockDAO, LocalDate date) {
        // Ensure CSV is loaded
        URL csvUrl = PortfolioInitializer.class.getResource("/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");
        if (csvUrl == null) {
            throw new RuntimeException("CSV resource not found.");
        }
        try {
            stockDAO.loadCSV(csvUrl.getFile());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load CSV.");
        }

        // Create the dummy portfolio with an initial balance.
        List<PortfolioEntry> holdings = new ArrayList<>();
        PortfolioDAO portfolioDAO = new PortfolioDAO(holdings, 10000);

        // Retrieve stock data for the given date.
        StockData stock1 = stockDAO.getStockData(StockName.WES_AX, date);
        StockData stock2 = stockDAO.getStockData(StockName.TLS_AX, date);
        StockData stock3 = stockDAO.getStockData(StockName.AMC_AX, date);

        if (stock1 == null || stock2 == null || stock3 == null) {
            throw new RuntimeException("One or more stock data entries are null for date: " + date);
        }

        // Create portfolio entries
        PortfolioEntry entry1 = new PortfolioEntry(StockName.WES_AX, stock1.getClose(), 1000);
        PortfolioEntry entry2 = new PortfolioEntry(StockName.TLS_AX, stock2.getClose(), 1000);
        PortfolioEntry entry3 = new PortfolioEntry(StockName.AMC_AX, stock3.getClose(), 1000);

        // Add entries to portfolio
        portfolioDAO.addToHoldings(entry1);
        portfolioDAO.addToHoldings(entry2);
        portfolioDAO.addToHoldings(entry3);

        return portfolioDAO;
    }
}
