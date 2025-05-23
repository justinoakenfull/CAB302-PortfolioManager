package com.javarepowizards.portfoliomanager.controllers.simulation;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.operations.simulation.PortfolioSimulation;
import com.javarepowizards.portfoliomanager.services.OllamaService;

import com.javarepowizards.portfoliomanager.services.PortfolioStatistics;
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

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;

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
    private Label labelReview;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ListView<String> listHoldings;

    // References that must be provided externally (from MainController, for example)
    private IPortfolioDAO portfolioDAO;
    private StockDAO stockDAO;
    private LocalDate mostRecentDate = LocalDate.of(2023, 12, 29);
    private final OllamaService ollamaService = new OllamaService();

    private static final Pattern TAGGED_RESPONSE = Pattern.compile("(?s)(?<=<Start>)(.*?)(?=<Finish>)");

    private static final String PROMPT_TEMPLATE= """
            You are an AI tutor/grader built into a student investment-simulator. After I running the simulation, you use these results:
            
            • Starting balance: $%.2f \s
            • Portfolio holdings:
            %s
            • Portfolio balance: $%.2f \s
            • Sharpe ratio: %.2f \s
            • Volatility: %.2f%% \s
            • Cumulative return: %.2f%% \s
            
            Now respond **exactly** in the structure below, with no extra greetings or commentary, wrapped in <Start> and <Finish> tags:
            
            <Start>
            Performance Summary & Review:
            {Your detailed, creative overview—beyond just stats}
            
            Highlights:
            - {Bullet-point insights}
            
            Tips for Improvement:
            - {Actionable, portfolio-specific advice}
            
            Rating:
            /10 with a brief comment
            <Finish>
            ""\";
            """;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.portfolioDAO = AppContext.getService(IPortfolioDAO.class);
        this.stockDAO = AppContext.getService(StockDAO.class);


        initializeUI();

        refreshPortfolioData();


        // Set run simulation button action.
        btnRunSimulation.setOnAction(e -> runSimulation());
        progressIndicator.setVisible(false);
    }

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
            labelPortfolioValue.setText(String.format("Portfolio: $%.2f", preValue));

            List<String> rows = portfolioDAO.getHoldings().stream()
                    .map(en -> String.format("%s: %d shares @ $%,.2f = $%,.2f",
                            en.getStock().getSymbol(),
                            (int) en.getAmountHeld(),
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

        if (!ollamaService.isServiceAvailable()) {           // ① guard clause
            showOllamaWarning();
            return;
        }

        int simulationDays = (int) sliderSimulationDays.getValue();
        PortfolioSimulation engine = buildSimEngine(simulationDays);

        prepareUiForSimulation();

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
    }

// ------------------------------------------------------------------
//   helpers
// ------------------------------------------------------------------

    /** Build the sim engine */
    private PortfolioSimulation buildSimEngine(int days) {
        double k  = 2.0, maxΔ = 0.02, α = 0.3;
        return new PortfolioSimulation(
                portfolioDAO, stockDAO, mostRecentDate,
                days, k, maxΔ, α);
    }

    /** What to do when simulation finishes without error. */
    private void onSimSuccess(List<Double> values, int days) {
        updateChart(values);                                   // graph
        PortfolioStatistics.Metrics m =
                PortfolioStatistics.compute(values, days);
        updateMetricLabels(m, values.get(values.size() - 1));  // numbers
        String prompt = buildPrompt(m, values.get(values.size() - 1));
        fetchAiSummary(prompt);                                // AI call
    }

    /** on simulation failure. */
    private void onSimFailure(Throwable ex) {
        ex.printStackTrace();
        labelReview.setText("⚠️ Simulation failed: " + ex.getMessage());
        btnRunSimulation.setDisable(false);
        progressIndicator.setVisible(false);
    }

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
        portfolioLineChart.getData().setAll(s);
    }

    /** Write numbers to the three metric labels + current value. */
    private void updateMetricLabels(PortfolioStatistics.Metrics m,
                                    double latestValue) {
        labelCumulativeReturn.setText("%.2f%%".formatted(m.cumulativeReturnPct()));
        labelVolatility      .setText("%.2f%%".formatted(m.annualisedVolatilityPct()));
        labelSharpeRatio     .setText("%.2f".formatted(m.annualisedSharpe()));
        labelPortfolioValue  .setText("Portfolio: $%,.2f".formatted(latestValue));
        // labelBalance        .setText("Balance: $%,.2f".formatted(portfolioDAO.getAvailableBalance())); Cash Balance need to come back
    }

    /** Builds the prompt for AI . */
    private String buildPrompt(PortfolioStatistics.Metrics m,
                               double finalBalance) {
        double starting = portfolioDAO.getAvailableBalance()
                + portfolioDAO.getTotalPortfolioValue();

        List<String> rows = portfolioDAO.getHoldings().stream()
                .map(en -> "%s: %d shares @ $%,.2f = $%,.2f".formatted(
                        en.getStock().getSymbol(),
                        (int) en.getAmountHeld(),
                        en.getPurchasePrice(),
                        en.getMarketValue()))
                .toList();

        return PROMPT_TEMPLATE.formatted(
                starting,
                String.join("\n", rows),
                finalBalance,
                m.annualisedSharpe(),
                m.annualisedVolatilityPct(),
                m.cumulativeReturnPct());
    }

    /** Ollama request, update UI on success/fail. */
    private void fetchAiSummary(String prompt) {
        Task<String> aiTask = new Task<>() {
            @Override protected String call() throws Exception {
                return ollamaService.generateResponse(prompt);
            }
        };

        aiTask.setOnSucceeded(e -> {
            labelReview.setText(extractCore(aiTask.getValue()));
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

    /**  when Ollama isn’t running. */
    private void showOllamaWarning() {
        labelReview.setText("""
        ⚠️ Ollama is not running.
        Start Ollama Desktop or `ollama serve` to receive the AI summary.
    """);
        labelReview.setStyle("-fx-text-fill:#FFFFFF;");
        progressIndicator.setVisible(false);
        btnRunSimulation.setDisable(false);
    }

    /** Extractrs the core of the Ai response, inbetween the tags*/
    public static String extractCore(String raw) {
        String startTag = "<Start>";
        String endTag = "<Finish>";

        int s = raw.indexOf(startTag);
        if (s >= 0){
            String after = raw.substring(s + startTag.length());
            int f = after.indexOf(endTag);
            if (f >= 0){
                return after.substring(0, f).trim();
            }else {
                return after.trim();
            }
        }
        return raw.trim();

    }


}
