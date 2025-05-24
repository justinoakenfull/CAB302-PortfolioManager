package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller for the Stocks view.
 * Responsible for:
 *   Initializing the stocks TableView with data and custom cell formatting.
 *   Handling user interactions such as buying a stock.
 *   Binding UI elements for responsive layout.
 */
public class StocksController implements Initializable {

    // --- FXML-injected UI components ---
    @FXML private TableView<StockRow> tableView;      // Main table of available stocks
    @FXML private TextField stockQuantityField;            // User input for quantity to buy
    @FXML private Label buyFeedbackLabel;             // Feedback label for buy actions
    @FXML private Button buyStockButton;              // Button to trigger buy operation
    @FXML private VBox portfolioBox;                  // Container for portfolio pie chart
    // @FXML private Label portfolioHeading;             // Heading label for portfolio pane
    @FXML private TableColumn<StockRow, Void> infoCol;
    @FXML private TableColumn<StockRow, Void> favouriteCol;
    @FXML private Label pricePerShareLabel;

    // --- Data access objects ---
    private IPortfolioDAO portfolioDAO;               // DAO for managing portfolio entries
    private StockRepository stockRepository;          // Repository for fetching stock data

    @Autowired
    // Data access objects and current user ID

    private IUserDAO userDAO;

    private IWatchlistDAO watchlistDAO;
    private int currentUserId;

    // Master list of all stocks for search filtering
    private final ObservableList<StockRow> allStocks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        watchlistDAO = AppContext.getService(IWatchlistDAO.class);
        IUserDAO userDAO = AppContext.getService(IUserDAO.class);

        // Determine the current user's ID (default to 1 if not present)
        currentUserId = userDAO.getCurrentUser().map(u -> u.getUserId()).orElse(1);

        // Clear existing columns and set up fresh ones
        tableView.getColumns().clear();
        // Retrieve application services
        stockRepository = AppContext.getService(StockRepository.class);
        portfolioDAO = AppContext.getService(IPortfolioDAO.class);

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
        TableColumn<StockRow, Long>   volumeCol    = new TableColumn<>("Volume (M)");

        openCol.setCellValueFactory(cell -> cell.getValue().openProperty().asObject());
        closeCol.setCellValueFactory(cell -> cell.getValue().closeProperty().asObject());
        changeCol.setCellValueFactory(cell -> cell.getValue().changeProperty().asObject());
        changePctCol.setCellValueFactory(cell -> cell.getValue().changePercentProperty().asObject());

        // Column for trading volume
        volumeCol.setCellValueFactory(cell -> cell.getValue().volumeProperty().asObject());


        // --- Cell formatting ---

        // Factory to format numbers to two decimal places, default white text
        Callback<TableColumn<StockRow, Double>, TableCell<StockRow, Double>> twoDecimalFactory =
                col -> new TableCell<>() {
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
        changePctCol.setCellFactory(col -> new TableCell<>() {
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
        tableView.getColumns().addAll(
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

        setupInfoColumn();
        setupFavouriteColumn();
        tableView.getColumns().addAll(favouriteCol, infoCol);


    }

    private void setupFavouriteColumn() {
        favouriteCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<StockRow, Void> call(final TableColumn<StockRow, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button();
                    private final ImageView unfavourited = new ImageView(
                            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/Unfavourited32x32.png"))));
                    private final ImageView favourite = new ImageView(
                            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/Favourited32x32.png"))));
                    {

                        unfavourited.setFitWidth(32);
                        unfavourited.setFitHeight(32);
                        favourite.setFitWidth(32);
                        favourite.setFitHeight(32);

                        btn.getStyleClass().add("image-button");
                        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        btn.setOnAction((ActionEvent event) -> {
                            StockRow data = getTableView().getItems().get(getIndex());
                            String ticker = data.tickerProperty().get();
                            StockName stockName = StockName.fromString(ticker);

                            try {
                                List<StockName> favorites = watchlistDAO.listForUser(currentUserId);
                                if (favorites.contains(stockName)) {
                                    watchlistDAO.removeForUser(currentUserId, stockName);
                                    btn.setGraphic(unfavourited);
                                    btn.setTooltip(new Tooltip("Add to favourites"));
                                } else {
                                    watchlistDAO.addForUser(currentUserId, stockName);
                                    btn.setGraphic(favourite);
                                    btn.setTooltip(new Tooltip("Remove from favourites"));
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            StockRow data = getTableView().getItems().get(getIndex());
                            String ticker = data.tickerProperty().get();
                            StockName stockName = StockName.fromString(ticker);

                            try {
                                List<StockName> favourites = watchlistDAO.listForUser(currentUserId);
                                boolean isFavourite = favourites.contains(stockName);

                                btn.setGraphic(isFavourite ? favourite : unfavourited);
                                btn.setTooltip(new Tooltip(isFavourite ? "Remove from favourites" : "Add to favourites"));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            setGraphic(btn);
                        }
                    }
                };
            }
        });
    }

    private void setupInfoColumn() {
        infoCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<StockRow, Void> call(final TableColumn<StockRow, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button();
                    {
                        ImageView imageView = new ImageView(
                                new Image(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/StockButtonAdd64x64.png")));
                        imageView.setFitWidth(32);
                        imageView.setFitHeight(32);
                        btn.setGraphic(imageView);
                        btn.getStyleClass().add("image-button");
                        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        btn.setOnAction((ActionEvent event) -> {
                            StockRow data = getTableView().getItems().get(getIndex());
                            tableView.getSelectionModel().select(data);
                            selectStocks();
                        });

                        // Tooltip for better UX
                        btn.setTooltip(new Tooltip("Check this stock out"));
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        });
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

            // Update in-memory DAO
            portfolioDAO.addToHoldings(entry);
            // Persist to database
            double totalValue = price * quantity;
            portfolioDAO.upsertHolding(currentUserId, stockName, quantity, totalValue);

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

    // Increases purchase amount by 1, defaults to 1 on invalid input
    @FXML
    private void increasePurchaseAmount() {
        try {
            int current = Integer.parseInt(stockQuantityField.getText().trim());
            stockQuantityField.setText(String.valueOf(current + 1));
        } catch (NumberFormatException e) {
            stockQuantityField.setText("1");
        }
        selectStocks();
    }

    // Decreases purchase amount by 1, minimum of 1, defaults to 1 on invalid input
    @FXML
    private void decreasePurchaseAmount() {
        try {
            int current = Integer.parseInt(stockQuantityField.getText().trim());
            if (current > 1) {
                stockQuantityField.setText(String.valueOf(current - 1));
            }
        } catch (NumberFormatException e) {
            stockQuantityField.setText("1");
        }
        selectStocks();
    }

    // Updates feedback label when a row is selected via info button
    @FXML
    private void selectStocks() {
        StockRow selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            buyFeedbackLabel.setText("No Stock Selected!");
            buyFeedbackLabel.setTextFill(Color.RED);
            buyFeedbackLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            pricePerShareLabel.setText(""); // Clear the price label
        } else {
            String name = selected.companyNameProperty().get();
            String ticker = selected.tickerProperty().get();
            double price = selected.closeProperty().get();
            buyFeedbackLabel.setText(name + " (" + ticker + ")");
            buyFeedbackLabel.setTextFill(Color.web("#1f75fe"));
            buyFeedbackLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            try {
                int quantity = Integer.parseInt(stockQuantityField.getText().trim());
                double total = price * quantity;
                pricePerShareLabel.setText(String.format("Total: $%.2f", total));
            } catch (NumberFormatException e) {
                pricePerShareLabel.setText("Total: $0.00");
            }
        }
    }





}
