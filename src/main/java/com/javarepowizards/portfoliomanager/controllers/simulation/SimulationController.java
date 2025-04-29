package com.javarepowizards.portfoliomanager.controllers.simulation;

import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.operations.simulation.PortfolioSimulation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {

    @FXML
    private LineChart<Number, Number> portfolioLineChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label labelBalance;
    @FXML
    private Label labelPortfolioValue;
    @FXML
    private Label labelSharpeRatio;
    @FXML
    private Label labelVolatility;
    @FXML
    private Label labelCumulativeReturn;

    @FXML
    private Slider sliderSimulationDays;
    @FXML
    private Button btnRunSimulation;
    @FXML
    private TextArea textReview;
    @FXML
    private ListView<String> listHoldings;

    // References that must be provided externally (from MainController, for example)
    private IPortfolioDAO portfolioDAO;
    private StockDAO stockDAO;
    private LocalDate mostRecentDate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set default labels and chart settings.
        labelBalance.setText("Balance: $0.00");
        labelPortfolioValue.setText("Portfolio: $0.00");
        labelSharpeRatio.setText("Sharpe Ratio: N/A");
        labelVolatility.setText("Volatility: N/A");
        labelCumulativeReturn.setText("Cumulative Return: N/A");
        xAxis.setLabel("Simulation Day");
        yAxis.setLabel("Portfolio Value ($)");

        // Set run simulation button action.
        btnRunSimulation.setOnAction(e -> runSimulation());
    }

    // Setter methods for dependencies.
    public void setPortfolioDAO(IPortfolioDAO portfolioDAO) {
        System.out.println("portfolio doa = " + portfolioDAO);
        this.portfolioDAO = portfolioDAO;
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public void setMostRecentDate(LocalDate mostRecentDate) {
        this.mostRecentDate = mostRecentDate;
    }

    private void runSimulation() {
        // Retrieve simulation days from the slider.
        int simulationDays = (int) sliderSimulationDays.getValue();

        // Define simulation parameters.
        double kMultiplier = 2.0;
        double maxDailyMovement = 0.05;
        double smoothingFactor = 0.3;

        // Create the PortfolioSimulationEngine.
        //
        PortfolioSimulation portfolioEngine = new PortfolioSimulation(
                portfolioDAO,      // The PortfolioDAO containing your dummy portfolio of 3 stocks.
                stockDAO,          // The StockDAO for fetching historical data.
                mostRecentDate,    // The most recent date (e.g., LocalDate.of(2023, 12, 29)).
                simulationDays,    // Number of days to simulate.
                kMultiplier,       // Multiplier for dynamic boundaries.
                maxDailyMovement,  // Maximum allowed daily movement percentage.
                smoothingFactor    // Smoothing factor for dynamic momentum updates.
        );

        // Run the simulation to obtain a list of portfolio values over time.
        List<Double> portfolioValues = portfolioEngine.simulatePortfolio();

        // Create a new data series for the LineChart.
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Portfolio Value");

        // Iterate over simulation results and add data points to the series.
        for (int day = 0; day < portfolioValues.size(); day++) {
            series.getData().add(new XYChart.Data<>(day, portfolioValues.get(day)));
        }

        // Clear any previous data from the LineChart and add the new series.
        portfolioLineChart.getData().clear();
        portfolioLineChart.getData().add(series);


    }

}
