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
import javafx.collections.transformation.FilteredList;
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
 * Initializing the stocks TableView with data and custom cell formatting.
 * Handling user interactions such as buying a stock.
 * Binding UI elements for responsive layout.
 */
public class StocksController implements Initializable {

    // --- FXML-injected UI components ---
    /**
     * Main table of available stocks
     */
    @FXML
    private TableView<StockRow> tableView;
    /**
     * User input for quantity to buy
     */
    @FXML
    private TextField stockQuantityField;
    /**
     * Feedback label for buy actions
     */
    @FXML
    private Label buyFeedbackLabel;
    /**
     * Button to trigger buy operation
     */
    @FXML
    private Button buyStockButton;
    /**
     * Container for portfolio pie chart
     */
    @FXML
    private VBox portfolioBox;
    /**
     * Column for info buttons
     */
    @FXML
    private TableColumn<StockRow, Void> infoCol;
    /**
     * Column for favourite toggle buttons
     */
    @FXML
    private TableColumn<StockRow, Void> favouriteCol;
    /**
     * Label for price per share
     */
    @FXML
    private Label pricePerShareLabel;
    /**
     * Label for available cash balance
     */
    @FXML
    private Label cashBalanceLabel;
    /**
     * Label for overall portfolio value
     */
    @FXML
    private Label portfolioValueLabel;
    /**
     * Label for search for stock
     */
    @FXML
    private TextField stockSearchField;

    // --- Data access objects ---
    /**
     * DAO for managing portfolio entries
     */
    private IPortfolioDAO portfolioDAO;
    /**
     * Repository for fetching stock data
     */
    private StockRepository stockRepository;

    /**
     * DAO for accessing user information
     */
    @Autowired
    private IUserDAO userDAO;
    /**
     * DAO for managing watchlist data
     */
    private IWatchlistDAO watchlistDAO;
    /**
     * Current logged-in user ID
     */
    private int currentUserId;

    /**
     * Master list of all stocks for search filtering
     */
    private final ObservableList<StockRow> allStocks = FXCollections.observableArrayList();

    /**
     * Initializes the controller and its UI components.
     * Sets up DAOs, retrieves initial user and stock data,
     * sets up table columns, and event handlers.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Retrieve services from application context
        watchlistDAO = AppContext.getService(IWatchlistDAO.class);
        IUserDAO userDAO = AppContext.getService(IUserDAO.class);

        // Determine the current user's ID (default to 1 if not present)
        currentUserId = userDAO.getCurrentUser().map(u -> u.getUserId()).orElse(1);

        // Clear existing columns and set up fresh ones
        tableView.getColumns().clear();

        // Retrieve application services
        stockRepository = AppContext.getService(StockRepository.class);
        portfolioDAO = AppContext.getService(IPortfolioDAO.class);

        // Display initial cash balance
        double balance = portfolioDAO.getAvailableBalance();
        updateCashBalanceLabel();
        cashBalanceLabel.setText(String.format("Cash: $%.2f", balance));
        updateBalanceLabels();

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

        // Factory for Change (%) column: append "%", color red/green
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

                // Compute percentage and style accordingly
                double pct = value;
                setText(String.format("%.2f%%", pct));
                getStyleClass().removeAll("balance-value-positive", "balance-value-negative");

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
            // Attempt to load available stocks into the table
            loadStocks();
            stockSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal.toLowerCase();
                filteredStocks.setPredicate(stock -> {
                    if (lower.isEmpty()) return true;
                    return stock.tickerProperty().get().toLowerCase().contains(lower)
                            || stock.companyNameProperty().get().toLowerCase().contains(lower);
                });
            });

        } catch (IOException e) {
            // Display error in feedback label if stock data cannot be loaded
            buyFeedbackLabel.setText("Error loading stock data");
            buyFeedbackLabel.setTextFill(Color.RED);
        }

        // --- Event handlers ---
        // Set action for the Buy button to trigger stock purchase logic
        buyStockButton.setOnAction(e -> handleBuyStock());

        // Initialize the info and favourite columns with custom cells
        setupInfoColumn();
        setupFavouriteColumn();

        // Add interactive columns to the table
        tableView.getColumns().addAll(favouriteCol, infoCol);
    }

    /**
     * Initializes the 'Favourite' column with toggleable star buttons.
     * Each cell contains a button to mark or unmark a stock as favourite.
     * Uses DAO methods to check and update user watchlist.
     */
    private void setupFavouriteColumn() {
        favouriteCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<StockRow, Void> call(final TableColumn<StockRow, Void> param) {
                return new TableCell<>() {
                    // Button to toggle favourite state
                    private final Button btn = new Button();
                    // Icons for favourited and unfavourited states
                    private final ImageView unfavourited = new ImageView(
                            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/Unfavourited32x32.png"))));
                    private final ImageView favourite = new ImageView(
                            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/Favourited32x32.png"))));

                    {
                        // Set icon sizes
                        unfavourited.setFitWidth(32);
                        unfavourited.setFitHeight(32);
                        favourite.setFitWidth(32);
                        favourite.setFitHeight(32);

                        // Style the button to only show the graphic
                        btn.getStyleClass().add("image-button");
                        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        // Define button action to toggle favourite state
                        btn.setOnAction((ActionEvent event) -> {
                            StockRow data = getTableView().getItems().get(getIndex());
                            String ticker = data.tickerProperty().get();
                            StockName stockName = StockName.fromString(ticker);

                            try {
                                List<StockName> favorites = watchlistDAO.listForUser(currentUserId);
                                if (favorites.contains(stockName)) {
                                    // Remove from favourites if already present
                                    watchlistDAO.removeForUser(currentUserId, stockName);
                                    btn.setGraphic(unfavourited);
                                    btn.setTooltip(new Tooltip("Add to favourites"));
                                } else {
                                    // Add to favourites
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
                            // Determine current stock and update button appearance
                            StockRow data = getTableView().getItems().get(getIndex());
                            String ticker = data.tickerProperty().get();
                            StockName stockName = StockName.fromString(ticker);

                            try {
                                List<StockName> favourites = watchlistDAO.listForUser(currentUserId);
                                boolean isFavourite = favourites.contains(stockName);

                                // Set appropriate graphic and tooltip
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

    /**
     * Initializes the 'Info' column with buttons to select a stock row.
     * Each cell includes a button that, when clicked, selects the corresponding
     * stock row and updates feedback labels with details.
     */
    private void setupInfoColumn() {
        infoCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<StockRow, Void> call(final TableColumn<StockRow, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button();

                    {
                        // Load and set icon for the button
                        ImageView imageView = new ImageView(
                                new Image(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/StockButtonAdd64x64.png")));
                        imageView.setFitWidth(32);
                        imageView.setFitHeight(32);
                        btn.setGraphic(imageView);
                        btn.getStyleClass().add("image-button");
                        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        // Set action to select the stock and show info
                        btn.setOnAction((ActionEvent event) -> {
                            StockRow data = getTableView().getItems().get(getIndex());
                            tableView.getSelectionModel().select(data);
                            selectStocks();
                        });

                        // Tooltip for user guidance
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

    private FilteredList<StockRow> filteredStocks;

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
            if (stock != null && stock.getCurrentRecord() != null) {
                rows.add(new StockRow(stock));
            }
        }

        allStocks.setAll(rows); // Keep a reference for filtering
        filteredStocks = new FilteredList<>(allStocks, p -> true);
        tableView.setItems(filteredStocks);
    }

    /**
     * Handles the Buy button action.
     * Validates the selected stock and quantity. Creates a PortfolioEntry
     * and updates DAO and UI accordingly.
     */
    private void handleBuyStock() {
        StockRow selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            buyFeedbackLabel.setText("No Stock Selected!");
            buyFeedbackLabel.setTextFill(Color.RED);
            return;
        }

        try {
            int quantity = Integer.parseInt(stockQuantityField.getText());

            StockName stockName = StockName.fromString(selected.tickerProperty().get());
            double price = selected.closeProperty().get();
            PortfolioEntry entry = new PortfolioEntry(stockName, price, quantity);

            double totalValue = price * quantity;
            double currentBalance = portfolioDAO.getAvailableBalance();

            if (totalValue > currentBalance) {
                buyFeedbackLabel.setText("Insufficient balance. You need $" + String.format("%.2f", totalValue));
                buyFeedbackLabel.setTextFill(Color.RED);
                return;
            }

            portfolioDAO.upsertHolding(currentUserId, stockName, quantity, totalValue);
            portfolioDAO.deductFromBalance(currentUserId, totalValue);

            // Refresh balance labels
            updateCashBalanceLabel();
            updateBalanceLabels();

            buyFeedbackLabel.setText("Bought " + quantity + " " + stockName.getSymbol());
            buyFeedbackLabel.setTextFill(Color.LIGHTGREEN);

        } catch (NumberFormatException ex) {
            buyFeedbackLabel.setText("Invalid quantity.");
            buyFeedbackLabel.setTextFill(Color.RED);

        } catch (Exception ex) {
            buyFeedbackLabel.setText("Error: " + ex.getMessage());
            buyFeedbackLabel.setTextFill(Color.RED);
        }
    }

    /**
     * Updates the label showing the user's available cash.
     */
    private void updateCashBalanceLabel() {
        double balance = portfolioDAO.getAvailableBalance();
        cashBalanceLabel.setText(String.format("Cash: $%.2f", balance));
    }

    /**
     * Increases the purchase amount by one, default to 1 if invalid.
     */
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

    /**
     * Decreases the purchase amount by one, not below 1.
     */
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

    /**
     * Updates UI when a stock is selected via the info button.
     */
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

    /**
     * Updates the labels showing cash and holdings values.
     */
    private void updateBalanceLabels() {
        double cash = portfolioDAO.getAvailableBalance();
        double total = portfolioDAO.getTotalPortfolioValue();
        double holdings = total - cash;

        cashBalanceLabel.setText(String.format("Cash: $%,.2f", cash));
        portfolioValueLabel.setText(String.format("Portfolio: $%,.2f", holdings));
    }
}

