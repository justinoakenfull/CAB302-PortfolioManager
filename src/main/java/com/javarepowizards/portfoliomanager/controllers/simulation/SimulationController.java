package com.javarepowizards.portfoliomanager.controllers.simulation;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.services.simulation.PortfolioSimulation;
import com.javarepowizards.portfoliomanager.services.simulation.ISimulationServices;
import com.javarepowizards.portfoliomanager.services.utility.OllamaService;
import com.javarepowizards.portfoliomanager.services.simulation.PortfolioStatistics;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the simulation view.
 * Sets up simulation services and UI components,
 * handles execution of portfolio simulations,
 * and retrieves AI summaries of simulation results.
 */
public class SimulationController implements Initializable {



    @FXML private LineChart<Number, Number> portfolioLineChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label labelBalance;
    @FXML private Label labelHoldings;
    @FXML private Label labelPortfolioValue;
    @FXML private Label labelSharpeRatio;
    @FXML private Label labelVolatility;
    @FXML private Label labelCumulativeReturn;
    @FXML private Slider sliderSimulationDays;
    @FXML private Button btnRunSimulation;
    @FXML private Label labelReview;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private ListView<String> listHoldings;

    private int buttonCount = 0;

    // References that must be provided externally (from MainController, for example)
    private IPortfolioDAO portfolioDAO;

    // OllamaService instance to handle AI interactions
    private final OllamaService ollamaService = new OllamaService();
    // SimulationServices instance to handle simulation logic
    private ISimulationServices services;


    /**
     * Initializes the controller with the necessary services and UI components.
     * This method is called by the JavaFX framework when the FXML file is loaded.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if no localization is needed.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.portfolioDAO = AppContext.getService(IPortfolioDAO.class);

        this.services = AppContext.getService(ISimulationServices.class);

        initializeUI();

        refreshPortfolioData();


        // Set run simulation button action.
        btnRunSimulation.setOnAction(e -> runSimulation());
        progressIndicator.setVisible(false);
    }

    /**
     * Initializes the UI components with default values and settings.
     * This method sets the initial text for labels, configures the axes of the chart,
     * and prepares the UI for displaying simulation results.
     */
    private void initializeUI(){
        // Set default labels and chart settings.
        labelBalance.setText("Balance: 0");
        labelPortfolioValue.setText("Portfolio:0 ");
        labelSharpeRatio.setText("Sharpe Ratio: N/A");
        labelVolatility.setText("Volatility: N/A");
        labelCumulativeReturn.setText("Cumulative Return: N/A");
        xAxis.setLabel("Simulation Day");
        yAxis.setLabel("Portfolio Value ($)");
    }

    /**
     * Refreshes the portfolio data displayed in the UI.
     * This method updates the balance, portfolio value, and holdings list.
     */
    public void refreshPortfolioData() {

        if (portfolioDAO != null) {
            double preValue = portfolioDAO.getTotalPortfolioValue();
            double preBalance = portfolioDAO.getAvailableBalance();
            labelBalance.setText(String.format("Balance: $%.2f", preBalance));
            labelHoldings.setText(String.format("Holdings $%.2f", preValue - preBalance));
            labelPortfolioValue.setText(String.format("Portfolio: $%.2f", preValue));


            List<String> rows = portfolioDAO.getHoldings().stream()
                    .map(en -> String.format("%s: %d shares @ $%,.2f = $%,.2f",
                            en.getStock().getSymbol(),
                            en.getAmountHeld(),
                            en.getPurchasePrice(),
                            en.getMarketValue()))
                    .collect(Collectors.toList());
            listHoldings.getItems().setAll(rows);
        }
    }

    /**
     * Runs the portfolio simulation and updates the UI with the results.
     * This method checks if the Ollama service is available, builds the simulation engine,
     * prepares the UI for simulation, and handles success or failure of the simulation.
     */
    private void runSimulation() {



        if (!ollamaService.isServiceAvailable()) {           //  guard clause
            showOllamaWarning();
            return;
        }
        prepareUiForSimulation();
        int simulationDays = (int) sliderSimulationDays.getValue();
        PortfolioSimulation engine = services.buildSimEngine(simulationDays);


        Task<List<Double>> simTask = new Task<>() {
            @Override protected List<Double> call() {
                return engine.simulatePortfolio();
            }
        };

        simTask.setOnSucceeded(evt ->
                onSimSuccess(simTask.getValue(), simulationDays));

        simTask.setOnFailed(evt ->
                onSimFailure(simTask.getException()));

        new Thread(simTask, "Sim-Thread").start();

        buttonCount = 0;
    }

// ------------------------------------------------------------------
//   helpers
// ------------------------------------------------------------------





    /** Disable button, show spinner, reset label. */
    private void prepareUiForSimulation() {
        btnRunSimulation.setDisable(true);
        labelReview.setText("Loading AI Summary…");
        labelReview.setStyle("-fx-text-fill:#FFFFFF;");
        progressIndicator.setVisible(true);
    }

    /** Draw the line chart. */
    private void updateChart(List<Double> vals) {
        XYChart.Series<Number,Number> s = new XYChart.Series<>();
        for (int d = 0; d < vals.size(); d++) {
            s.getData().add(new XYChart.Data<>(d, vals.get(d)));
        }

        portfolioLineChart.getData().setAll(List.of(s));
    }

    /** Write numbers to the three metric labels + current value. */
    private void updateMetricLabels(PortfolioStatistics.Metrics m,
                                    double latestValue) {
        labelCumulativeReturn.setText("%.2f%%".formatted(m.cumulativeReturnPct()));
        labelVolatility      .setText("%.2f%%".formatted(m.annualisedVolatilityPct()));
        labelSharpeRatio     .setText("%.2f".formatted(m.annualisedSharpe()));
        labelPortfolioValue  .setText("Portfolio: $%,.2f".formatted(latestValue));
        labelHoldings        .setText("Holdings: $%,.2f".formatted(latestValue - portfolioDAO.getAvailableBalance()));
    }



    /** Ollama request, update UI on success/fail. */
    private void fetchAiSummary(String prompt) {
        Task<String> aiTask = new Task<>() {
            @Override protected String call() throws Exception {
                return ollamaService.generateResponse(prompt);
            }
        };

        aiTask.setOnSucceeded(e -> {
            labelReview.setText(services.extractCore(aiTask.getValue()));
            progressIndicator.setVisible(false);
            btnRunSimulation.setDisable(false);
        });

        aiTask.setOnFailed(e -> {
            labelReview.setText("⚠️ AI call failed: " + aiTask.getException().getMessage());
            progressIndicator.setVisible(false);
            btnRunSimulation.setDisable(false);
        });

        new Thread(aiTask, "Ollama-Thread").start();
    }


    /** on simulation success. */
    private void onSimSuccess(List<Double> values, int days) {
        updateChart(values);                                   // graph
        PortfolioStatistics.Metrics m =
                PortfolioStatistics.compute(values, days);
        updateMetricLabels(m, values.getLast());  // numbers
        String prompt = services.buildPrompt(m, values.getLast());
        fetchAiSummary(prompt);                                // AI call
    }

    /** on simulation failure. */
    private void onSimFailure(Throwable ex) {
        labelReview.setText("⚠️ Simulation failed: " + ex.getMessage());
        btnRunSimulation.setDisable(false);
        progressIndicator.setVisible(false);
    }

    /**  when Ollama isn’t running. */
    private void showOllamaWarning() {

        labelReview.setText("""
        ⚠️ Ollama is not running.
        Start Ollama Desktop or `ollama serve` to receive the AI summary.
        Please try again. Failed Attempts:\s""" + ++buttonCount + ".");
        labelReview.setStyle("-fx-text-fill:#FFFFFF;");
        progressIndicator.setVisible(false);
        btnRunSimulation.setDisable(false);
    }
}
