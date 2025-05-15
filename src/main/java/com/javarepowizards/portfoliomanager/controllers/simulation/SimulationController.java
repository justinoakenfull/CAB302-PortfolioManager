package com.javarepowizards.portfoliomanager.controllers.simulation;

import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.operations.simulation.PortfolioSimulation;
import com.javarepowizards.portfoliomanager.services.OllamaService;

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
    private LocalDate mostRecentDate;
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
        // Set default labels and chart settings.
        labelBalance.setText("Balance: 0");
        labelPortfolioValue.setText("Portfolio:0 ");
        labelSharpeRatio.setText("Sharpe Ratio: N/A");
        labelVolatility.setText("Volatility: N/A");
        labelCumulativeReturn.setText("Cumulative Return: N/A");
        xAxis.setLabel("Simulation Day");
        yAxis.setLabel("Portfolio Value ($)");

        // Set run simulation button action.
        btnRunSimulation.setOnAction(e -> runSimulation());
        progressIndicator.setVisible(false);
    }

    // Setter methods for dependencies.
    public void setPortfolioDAO(IPortfolioDAO portfolioDAO) {
        this.portfolioDAO = portfolioDAO;
        refreshPortfolioData();
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public void setMostRecentDate(LocalDate mostRecentDate) {
        this.mostRecentDate = mostRecentDate;
    }


    public void refreshPortfolioData() {
        if (portfolioDAO != null) {
            double preValue = portfolioDAO.getTotalPortfolioValue();
            double preBalance = portfolioDAO.getAvailableBalance();
            labelBalance.setText(String.format("Balance: $%.2f", preBalance));
            labelPortfolioValue.setText(String.format("Portfolio: $%.2f", preValue));

            List<String> rows = portfolioDAO.getHoldings().stream()
                    .map(en -> String.format("%s: %.0f shares @ $%,.2f = $%,.2f",
                            en.getStock().getSymbol(),
                            en.getAmountHeld(),
                            en.getPurchasePrice(),
                            en.getMarketValue()))
                    .collect(Collectors.toList());
            listHoldings.getItems().setAll(rows);
        }
    }
    private void runSimulation() {




        // Retrieve simulation days from the slider.
        int simulationDays = (int) sliderSimulationDays.getValue();

        // Define simulation parameters.
        double kMultiplier = 2.0;
        double maxDailyMovement = 0.02;
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


        double start = portfolioValues.get(0);
        double end = portfolioValues.get(portfolioValues.size() - 1);
        double cumulativeReturn = ((end - start) / start) * 100;


        List<Double> rets = new ArrayList<>();
        for (int i = 1; i < portfolioValues.size(); i++) {
            double r = (portfolioValues.get(i) - portfolioValues.get(i-1)) / portfolioValues.get(i-1);
            rets.add(r);
        }
        double avgRet = rets.stream().mapToDouble(d->d).average().orElse(0.0);
        double annualisedRet = avgRet * simulationDays;

        double variance = rets.stream()
                .mapToDouble(d -> Math.pow(d - avgRet, 2))
                .average()
                .orElse(0.0);

        double vol    = Math.sqrt(variance);
        double annualisedVol = vol * Math.sqrt(simulationDays);

        double annualisedSharpe = annualisedRet / annualisedVol;

        labelCumulativeReturn.setText(String.format("%.2f%%", cumulativeReturn));
        labelVolatility       .setText(String.format("%.2f%%", annualisedVol * 100));
        labelSharpeRatio      .setText(String.format("%.2f",   annualisedSharpe));
        labelPortfolioValue   .setText(String.format("Portfolio: $%,.2f", end));

        double preBalance = portfolioDAO.getAvailableBalance();
        double preValue = portfolioDAO.getTotalPortfolioValue();


        // Prepare UI
        btnRunSimulation.setDisable(true);
        labelReview.setText("Loading AI Summary...");
        labelReview.setStyle("-fx-text-fill: #FFFFFF;");
        progressIndicator.setVisible(true);


        List<String> rows = portfolioDAO.getHoldings().stream()
                .map(en -> String.format("%s: %.0f shares @ $%,.2f = $%,.2f",
                        en.getStock().getSymbol(),
                        en.getAmountHeld(),
                        en.getPurchasePrice(),
                        en.getMarketValue()))
                .collect(Collectors.toList());


        String prompt = String.format(
                PROMPT_TEMPLATE,
                preBalance + preValue,       // e.g. 81755.59
                rows,          // the String from step 1
                end + preBalance,         // e.g. 76659.42
                annualisedSharpe,           // e.g. –0.47
                annualisedVol * 100,      // e.g. 12.17
                cumulativeReturn // e.g. –6.23
        );


        // Create background task for ai
        Task<String> aiTask = new Task<>() {
           @Override
            protected String call() throws Exception {
               return ollamaService.generateResponse(prompt);
           }
        };

        // On success update text are and reset UI
        aiTask.setOnSucceeded(evt -> {
            String raw = aiTask.getValue();
            String core = extractCore(raw);


            labelReview.setText(core);

            btnRunSimulation.setDisable(false);
            progressIndicator.setVisible(false);
        });

        // on failure
        aiTask.setOnFailed(evt -> {
            Throwable ex = aiTask.getException();
            ex.printStackTrace();
            textReview.setText("⚠️ AI call failed: " + aiTask.getException().getMessage());
            btnRunSimulation.setDisable(false);
            progressIndicator.setVisible(false);
        });


        new Thread(aiTask, "Ollama-Service-Thread").start();

    }

    private void updateOverview(double portfolioValue, double balance){
        labelPortfolioValue.setText(String.format("Portfolio: $%.2f", portfolioValue));
        labelBalance.setText(String.format("Balance: $%.2f", balance));
    }


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
