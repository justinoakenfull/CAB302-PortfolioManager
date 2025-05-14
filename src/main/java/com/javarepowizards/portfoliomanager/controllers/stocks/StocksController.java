package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Controller for the Stocks view.
 * Responsible for:
 *   Initializing the stocks TableView with data and custom cell formatting.
 *   Handling user interactions such as buying a stock.
 *   Binding UI elements for responsive layout.
 */
public class StocksController {

    // --- FXML-injected UI components ---
    @FXML private TableView<StockRow> tableView;      // Main table of available stocks
    @FXML private TextField stockQuantityField;            // User input for quantity to buy
    @FXML private Label buyFeedbackLabel;             // Feedback label for buy actions
    @FXML private Button buyStockButton;              // Button to trigger buy operation
    @FXML private VBox portfolioBox;                  // Container for portfolio pie chart
    // @FXML private Label portfolioHeading;             // Heading label for portfolio pane


    // --- Data access objects ---
    private PortfolioDAO portfolioDAO;                // DAO for managing portfolio entries
    private StockRepository stockRepository;          // Repository for fetching stock data

    /**
     * Called automatically after FXML fields are injected.
     * Performs the following initialization steps:
     *   Retrieve services (StockRepository, PortfolioDAO) from application context.
     *   Create and configure TableColumn objects.
     *   Install custom cell factories for formatting numeric values and coloring Change (%).
     *   Load stock data into the table.
     *   Set up event handler for the Buy button.
     *   Bind the portfolio heading width for responsive wrapping/ellipsis.
     * 
     * 
     */
    @FXML
    public void initialize() {
        // Retrieve application services
        this.stockRepository = AppContext.getService(StockRepository.class);
        this.portfolioDAO    = AppContext.getService(PortfolioDAO.class);

        // --- TableColumn setup ---

        // Column for stock ticker symbol
        TableColumn<StockRow, String> tickerCol = new TableColumn<>("Ticker");
        tickerCol.setCellValueFactory(cell -> cell.getValue().tickerProperty());

        // Column for company name
        TableColumn<StockRow, String> nameCol = new TableColumn<>("Stock");
        nameCol.setCellValueFactory(cell -> cell.getValue().companyNameProperty());

        // Column for opening price
        TableColumn<StockRow, Double> openCol = new TableColumn<>("Open");
        openCol.setCellValueFactory(cell -> cell.getValue().openProperty().asObject());

        // Column for closing price
        TableColumn<StockRow, Double> closeCol = new TableColumn<>("Close");
        closeCol.setCellValueFactory(cell -> cell.getValue().closeProperty().asObject());

        // Column for absolute change in price
        TableColumn<StockRow, Double> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(cell -> cell.getValue().changeProperty().asObject());

        // Column for percentage change
        TableColumn<StockRow, Double> changePctCol = new TableColumn<>("Change (%)");
        changePctCol.setCellValueFactory(cell -> cell.getValue().changePercentProperty().asObject());

        // Column for trading volume
        TableColumn<StockRow, Long> volumeCol = new TableColumn<>("Volume (M)");
        volumeCol.setCellValueFactory(cell -> cell.getValue().volumeProperty().asObject());

        // --- Cell formatting ---

        // Factory to format numbers to two decimal places, default white text
        Callback<TableColumn<StockRow, Double>, TableCell<StockRow, Double>> twoDecimalFactory =
                col -> new TableCell<StockRow, Double>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);

                        if (empty || value == null) {
                            // Clear text when row is empty
                            setText(null);
                            setTextFill(null);
                        } else {
                            // Format to two decimals
                            setText(String.format("%.2f", value));
                            setTextFill(Color.WHITE);
                        }
                    }
                };

        // Apply two-decimal formatting to Open, Close, and Change columns
        openCol.setCellFactory(twoDecimalFactory);
        closeCol.setCellFactory(twoDecimalFactory);
        changeCol.setCellFactory(twoDecimalFactory);

        // Factory for Change (%) column: append “%”, color red/green
        changePctCol.setCellFactory(col -> new TableCell<StockRow, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    // Clear on empty rows
                    setText(null);
                    getStyleClass().removeAll("balance-value-positive", "balance-value-negative");
                    return;
                }

                // Compute percentage
                double pct = value;
                setText(String.format("%.2f%%", pct));

                // Reset any previous style classes
                getStyleClass().removeAll("balance-value-positive", "balance-value-negative");

                // Tag with CSS class for coloring
                if (pct < 0) {
                    getStyleClass().add("balance-value-negative");
                } else {
                    getStyleClass().add("balance-value-positive");
                }
            }
        });

        // Attach all columns to the TableView
        tableView.getColumns().setAll(
                tickerCol, nameCol,
                openCol, closeCol,
                changeCol, changePctCol,
                volumeCol
        );

        // --- Data loading ---
        try {
            loadStocks();
        } catch (IOException e) {
            // Display error in feedback label if stock data cannot be loaded
            buyFeedbackLabel.setText("Error loading stock data");
            buyFeedbackLabel.setTextFill(Color.RED);
        }

        // --- Event handlers ---
         buyStockButton.setOnAction(e -> handleBuyStock());
    }

    /**
     * Loads stock data from the repository and populates the TableView.
     *
     * @throws IOException if there is a problem fetching or reading stock data
     */
    private void loadStocks() throws IOException {
        ObservableList<StockRow> rows = FXCollections.observableArrayList();

        // Iterate over all available tickers
        for (String ticker : stockRepository.availableTickers()) {
            IStock stock = stockRepository.getByTicker(ticker);

            // Only add rows for stocks with valid data
            if (stock != null && stock.getCurrentRecord() != null) {
                rows.add(new StockRow(stock));
            }
        }

        // Populate the table
        tableView.setItems(rows);
    }

    /**
     * Handles the “Buy” button action.
     * Validates the selected stock and quantity, then creates a PortfolioEntry
     * and saves it via the PortfolioDAO. Feedback is shown in the buyFeedbackLabel.
     */
    private void handleBuyStock() {
        StockRow selected = tableView.getSelectionModel().getSelectedItem();

        // If no stock is selected, show error
        if (selected == null) {
            buyFeedbackLabel.setText("No Stock Selected!");
            buyFeedbackLabel.setTextFill(Color.RED);
            return;
        }

        try {
            // Parse user-entered quantity
            int quantity = Integer.parseInt(stockQuantityField.getText());

            // Build a PortfolioEntry and persist it
            StockName stockName = StockName.fromString(selected.tickerProperty().get());
            double price = selected.closeProperty().get();
            PortfolioEntry entry = new PortfolioEntry(stockName, price, quantity);
            portfolioDAO.addToHoldings(entry);

            // Success feedback
            buyFeedbackLabel.setText("Bought " + quantity + " " + stockName.getSymbol());
            buyFeedbackLabel.setTextFill(Color.LIGHTGREEN);

        } catch (NumberFormatException ex) {
            // Handle invalid number input
            buyFeedbackLabel.setText("Invalid quantity.");
            buyFeedbackLabel.setTextFill(Color.RED);

        } catch (Exception ex) {
            // Handle other persistence errors
            buyFeedbackLabel.setText("Error: " + ex.getMessage());
            buyFeedbackLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void increasePurchaseAmount() {

    }

    @FXML
    public void decreasePurchaseAmount(ActionEvent actionEvent) {
    }
}
