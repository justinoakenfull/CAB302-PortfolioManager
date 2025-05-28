package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;

import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import javafx.util.Callback;
import javafx.scene.control.TableCell;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;

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

    private TableColumn<StockRow,String>  tickerCol, nameCol;
    private TableColumn<StockRow,Double>  openCol, closeCol, changeCol, changePctCol;
    private TableColumn<StockRow,Long>    volumeCol;

    /**
     * Initializes the controller with necessary services and data.
     * Sets up the TableView columns, cell factories, and event handlers.
     * Loads stock data and applies filtering based on user input.
     *
     * @param url  the location used to resolve relative paths for the root object,
     *             or null if the location is not known
     * @param rb   the resources used to localize the root object,
     *             or null if the root object does not need localization
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initServices();
        initCurrentUser();
        initLabels();
        initTableColumns();
        initCellFactories();
        initDataLoading();
        initEventHandlers();
        initCustomColumns();
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
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/StockButtonAdd64x64.png"))));
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


    private void initServices(){
        watchlistDAO = AppContext.getService(IWatchlistDAO.class);

        stockRepository = AppContext.getService(StockRepository.class);
        portfolioDAO = AppContext.getService(IPortfolioDAO.class);
    }

    private void initCurrentUser() {
        IUserDAO userDAO = AppContext.getService(IUserDAO.class);
        currentUserId = userDAO.getCurrentUser()
                .map(u -> u.getUserId())
                .orElse(1);
    }

    private void initLabels(){
        // Display initial cash balance
        double balance = portfolioDAO.getAvailableBalance();
        updateCashBalanceLabel();
        cashBalanceLabel.setText(String.format("Cash: $%.2f", balance));
        updateBalanceLabels();
    }

    private void initTableColumns() {
        // Clear existing columns and set up fresh ones
        tableView.getColumns().clear();

        tickerCol    = createColumn("Ticker", StockRow::tickerProperty);
        nameCol      = createColumn("Stock",  StockRow::companyNameProperty);
        openCol      = createColumn("Open",   row -> row.openProperty().asObject());
        closeCol     = createColumn("Close",  row -> row.closeProperty().asObject());
        changeCol    = createColumn("Change", row -> row.changeProperty().asObject());
        changePctCol = createColumn("Change (%)", row -> row.changePercentProperty().asObject());
        volumeCol    = createColumn("Volume (M)", row -> row.volumeProperty().asObject());


        // Attach all columns to the TableView
        tableView.getColumns().addAll(
                tickerCol, nameCol,
                openCol, closeCol,
                changeCol, changePctCol,
                volumeCol
        );
    }

    private <S,T> TableColumn<S,T> createColumn(
            String title,
            Function<S, ObservableValue<T>> propExtractor)
    {
        TableColumn<S,T> col = new TableColumn<>(title);
        // this lambda signature is important so Java knows you’re implementing
        // Callback<CellDataFeatures<S,T>, ObservableValue<T>>
        col.setCellValueFactory((TableColumn.CellDataFeatures<S,T> cell) ->
                propExtractor.apply(cell.getValue())
        );
        return col;
    }


    private void initCellFactories() {
        // two‐decimal factory
        Callback<TableColumn<StockRow,Double>,TableCell<StockRow,Double>> twoDec =
                col -> new TableCell<>() {
                    @Override protected void updateItem(Double v, boolean empty) {
                        super.updateItem(v, empty);
                        if (empty||v==null) {
                            setText(null); setTextFill(null);
                        } else {
                            setText(String.format("%.2f",v));
                            setTextFill(Color.WHITE);
                        }
                    }
                };

        openCol.setCellFactory(twoDec);
        closeCol.setCellFactory(twoDec);
        changeCol.setCellFactory(twoDec);

        // percent‐change factory
        changePctCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double pct, boolean empty) {
                super.updateItem(pct, empty);
                if (empty||pct==null) {
                    setText(null);
                    getStyleClass().removeAll("balance-value-positive","balance-value-negative");
                } else {
                    setText(String.format("%.2f%%",pct));
                    getStyleClass().removeAll("balance-value-positive","balance-value-negative");
                    getStyleClass().add(pct<0
                            ? "balance-value-negative"
                            : "balance-value-positive");
                }
            }
        });
    }

    private void initDataLoading() {
        try {
            loadStocks();
            stockSearchField.textProperty().addListener((obs,o,n) ->
                    filteredStocks.setPredicate(stock -> {
                        String lower = n.toLowerCase();
                        if (lower.isEmpty()) return true;
                        return stock.tickerProperty().get().toLowerCase().contains(lower)
                                || stock.companyNameProperty().get().toLowerCase().contains(lower);
                    })
            );
        } catch (IOException e) {
            buyFeedbackLabel.setText("Error loading stock data");
            buyFeedbackLabel.setTextFill(Color.RED);
        }
    }

    private void initEventHandlers() {
        buyStockButton.setOnAction(e -> handleBuyStock());
    }

    private void initCustomColumns() {
        setupInfoColumn();
        setupFavouriteColumn();
        tableView.getColumns().addAll(favouriteCol, infoCol);
    }


}

