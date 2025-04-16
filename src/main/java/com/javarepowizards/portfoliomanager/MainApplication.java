package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.operations.simulation.MarketSimulator;
import com.javarepowizards.portfoliomanager.operations.simulation.PortfolioSimulation;
import com.javarepowizards.portfoliomanager.services.StockDataFilter;
import com.javarepowizards.portfoliomanager.services.StockStatistics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();

        // Attempt to retrieve the CSV file as a resource using an absolute path.
        // This searches for the file starting from the root of the classpath.
        URL url = MainApplication.class.getResource("/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");

        // Check whether the resource was found. If the resource is not located,
        // url will be null and we throw a RuntimeException to indicate the missing file.
        if (url == null) {
            throw new RuntimeException("Resource not found: /com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");
        }

        // From the URL, obtain the file path as a String. This file path will be passed
        // to the StockDAO so it can open and parse the CSV file.
        String csvFilePath = url.getFile();  // csvFilePath now holds the full path to the CSV file.

        // Instantiate the StockDAO, which is responsible for reading and parsing the CSV file
        // into a collection of StockData objects mapped by StockName.
        StockDAO stockDAO = new StockDAO();

        // Attempt to load the CSV data using the loadCSV method of the DAO.
        // If an I/O error occurs during file reading, catch the exception, print the error,
        // and terminate further processing by returning from the method.
        try {
            stockDAO.loadCSV(csvFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Retrieve a list of all StockData entries associated with the stock WES.AX.
        // The getStockData method uses the StockName enum (in this case, StockName.WES_AX) to filter the data.
        List<StockData> wesData = stockDAO.getStockData(StockName.WES_AX);

        // Output a header message to the console showing which stock's dates are about to be printed.
        System.out.println("All dates for " + StockName.WES_AX.getSymbol() + ":");
        // Iterate over each StockData record for WES.AX and print its date.
        // This is useful for verifying which dates have been loaded from the CSV.
        for (StockData data : wesData) {
            System.out.println(data.getDate());
        }

        // Define a specific date for which we want to query the stock data.
        // In this example, we are using December 29, 2023.
        LocalDate date = LocalDate.of(2023, 12, 29);

        // Iterate over an array of stocks (WES.AX, TLS.AX, and AMC.AX) to retrieve data for each.
        // The StockName enum provides type-safety and consistency when referring to stock symbols.
        for (StockName stock : new StockName[]{StockName.WES_AX, StockName.TLS_AX, StockName.AMC_AX}) {
            // Print a header indicating which stock's data is being processed.
            System.out.println("Data for " + stock.getSymbol() + ":");

            // Retrieve the stock data for the current stock on the specified date.
            // This call uses a DAO method that returns a single StockData object for the given StockName and date.
            StockData stockData = stockDAO.getStockData(stock, date);

            // Check if a StockData record was found for the current stock on that date.
            if (stockData != null) {
                // If data is found, print out the "Open" price. Price is currently null.
                // #TODO Decide which price to use.
                System.out.println("Close for " + stock.getSymbol() + " on " + date + ": " + stockData.getClose());
            } else {
                // If no matching data is available, print a message indicating that.
                System.out.println("No data available for " + stock.getSymbol() + " on " + date);
            }

            // Print a separator line to clearly delineate output for each stock.
            System.out.println("------------------------------------------------");
        }

        // #TODO Add Holdings to User Account Feild, so we can go user.Holdings.
        //Current holdings
        List<PortfolioEntry> holdings = new ArrayList<>();
        // New portfolio
        PortfolioDAO portfolio = new PortfolioDAO(holdings, 10000);
        StockData stock1 = stockDAO.getStockData(StockName.WES_AX, date);
        StockData stock2 = stockDAO.getStockData(StockName.TLS_AX, date);
        StockData stock3 = stockDAO.getStockData(StockName.AMC_AX, date);

        PortfolioEntry entry1 = new PortfolioEntry(StockName.WES_AX,stock1.getClose(), 1000);
        PortfolioEntry entry2 = new PortfolioEntry(StockName.TLS_AX,stock2.getClose(), 1000);
        PortfolioEntry entry3 = new PortfolioEntry(StockName.AMC_AX,stock3.getClose(), 1000);
        portfolio.addToHoldings(entry1);
        portfolio.addToHoldings(entry2);
        portfolio.addToHoldings(entry3);

        System.out.println("----- Portfolio Holdings -----");
        for (PortfolioEntry holding : portfolio.getHoldings()) {
            System.out.println(holding);
        }
        System.out.println("Initial Portfolio Value: $" + portfolio.getTotalPortfolioValue());

        // Simulation settings.
        int simulationDays = 30;
        double kMultiplier = 2.0;
        double maxDailyMovement = 0.05;
        double smoothingFactor = 0.3;
        LocalDate mostRecentDate = date; // Re-use our defined date.

        // Create and run the PortfolioSimulationEngine.
        PortfolioSimulation portfolioEngine = new PortfolioSimulation(
                portfolio, stockDAO, mostRecentDate,
                simulationDays, kMultiplier, maxDailyMovement, smoothingFactor
        );
        List<Double> portfolioSimValues = portfolioEngine.simulatePortfolio();

        // Print the simulated portfolio values.
        System.out.println("----- Simulated Portfolio Values -----");
        for (int day = 0; day < portfolioSimValues.size(); day++) {
            System.out.println("Day " + day + ": " + portfolioSimValues.get(day));
        }




    }

    public static void main(String[] args) {
        launch();
    }
}