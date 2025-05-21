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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

// Controller for the Stocks view: initializes the stocks table, handles buying, search, and favourites.
public class StocksController implements Initializable {

    // FXML-injected UI components
    @FXML private TableView<StockRow> tableView;
    @FXML private TextField stockQuantityField;
    @FXML private Label buyFeedbackLabel;
    @FXML private Button buyStockButton;
    @FXML private VBox portfolioBox;
    @FXML private TableColumn<StockRow, Void> infoCol;
    @FXML private TableColumn<StockRow, Void> favouriteCol;
    @FXML private TextField stockSearchField;

    // Data access objects and current user ID
    private IPortfolioDAO portfolioDAO;
    private IUserDAO userDAO;
    private StockRepository stockRepository;
    private IWatchlistDAO watchlistDAO;
    private int currentUserId;

    // Master list of all stocks for search filtering
    private final ObservableList<StockRow> allStocks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Retrieve DAOs from the application context
        watchlistDAO    = AppContext.getService(IWatchlistDAO.class);
        userDAO         = AppContext.getService(IUserDAO.class);
        portfolioDAO = AppContext.getService(IPortfolioDAO.class);
        stockRepository = AppContext.getService(StockRepository.class);

        // Determine the current user's ID (default to 1 if not present)
        currentUserId = userDAO.getCurrentUser().map(u -> u.getUserId()).orElse(1);

        // Clear existing columns and set up fresh ones
        tableView.getColumns().clear();
        setupColumns();

        // Load stock data into the table
        try {
            loadStocks();
        } catch (IOException e) {
            buyFeedbackLabel.setText("Error loading stock data");
            buyFeedbackLabel.setTextFill(Color.RED);
        }

        // Configure the Buy button and additional UI handlers
        buyStockButton.setOnAction(e -> handleBuyStock());
        setupInfoColumn();
        setupFavouriteColumn();
        setupSearchFunctionality();
    }

    // Defines and attaches all columns to the stocks table
    private void setupColumns() {
        // Ticker symbol column
        TableColumn<StockRow, String> tickerCol = new TableColumn<>("Ticker");
        tickerCol.setCellValueFactory(cell -> cell.getValue().tickerProperty());

        // Company name column
        TableColumn<StockRow, String> nameCol = new TableColumn<>("Stock");
        nameCol.setCellValueFactory(cell -> cell.getValue().companyNameProperty());

        // Price and change columns
        TableColumn<StockRow, Double> openCol      = new TableColumn<>("Open");
        TableColumn<StockRow, Double> closeCol     = new TableColumn<>("Close");
        TableColumn<StockRow, Double> changeCol    = new TableColumn<>("Change");
        TableColumn<StockRow, Double> changePctCol = new TableColumn<>("Change (%)");
        TableColumn<StockRow, Long>   volumeCol    = new TableColumn<>("Volume (M)");

        openCol.setCellValueFactory(cell -> cell.getValue().openProperty().asObject());
        closeCol.setCellValueFactory(cell -> cell.getValue().closeProperty().asObject());
        changeCol.setCellValueFactory(cell -> cell.getValue().changeProperty().asObject());
        changePctCol.setCellValueFactory(cell -> cell.getValue().changePercentProperty().asObject());
        volumeCol.setCellValueFactory(cell -> cell.getValue().volumeProperty().asObject());

        // Factory for formatting numbers to two decimals
        Callback<TableColumn<StockRow, Double>, TableCell<StockRow, Double>> twoDecimalFactory =
                col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);
                        if (empty || value == null) {
                            setText(null);
                            setTextFill(null);
                        } else {
                            setText(String.format("%.2f", value));
                            setTextFill(Color.WHITE);
                        }
                    }
                };
        openCol.setCellFactory(twoDecimalFactory);
        closeCol.setCellFactory(twoDecimalFactory);
        changeCol.setCellFactory(twoDecimalFactory);

        // Factory for percentage column with color styling
        changePctCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    getStyleClass().removeAll("balance-value-positive", "balance-value-negative");
                } else {
                    double pct = value;
                    setText(String.format("%.2f%%", pct));
                    getStyleClass().removeAll("balance-value-positive", "balance-value-negative");
                    getStyleClass().add(pct < 0 ? "balance-value-negative" : "balance-value-positive");
                }
            }
        });

        // Attach all columns including the favourites and info columns
        tableView.getColumns().addAll(
                tickerCol, nameCol,
                openCol, closeCol,
                changeCol, changePctCol,
                volumeCol,
                favouriteCol, infoCol
        );
    }

    // Loads all stocks from the repository into the master list and table
    private void loadStocks() throws IOException {
        allStocks.clear();
        for (String ticker : stockRepository.availableTickers()) {
            IStock stock = stockRepository.getByTicker(ticker);
            if (stock != null && stock.getCurrentRecord() != null) {
                allStocks.add(new StockRow(stock));
            }
        }
        tableView.setItems(FXCollections.observableArrayList(allStocks));
    }

    // Handles the Buy button: updates both in-memory and persistent storage
    private void handleBuyStock() {
        StockRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            buyFeedbackLabel.setText("No Stock Selected!");
            buyFeedbackLabel.setTextFill(Color.RED);
            return;
        }
        try {
            int quantity = Integer.parseInt(stockQuantityField.getText().trim());
            StockName stockName = StockName.fromString(selected.tickerProperty().get());
            double price = selected.closeProperty().get();
            PortfolioEntry entry = new PortfolioEntry(stockName, price, quantity);

            // Update in-memory DAO
            portfolioDAO.addToHoldings(entry);
            // Persist to database
            double totalValue = price * quantity;
            portfolioDAO.upsertHolding(currentUserId, stockName, quantity, totalValue);

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

    // Increases purchase amount by 1, defaults to 1 on invalid input
    @FXML
    private void increasePurchaseAmount() {
        try {
            int current = Integer.parseInt(stockQuantityField.getText().trim());
            stockQuantityField.setText(String.valueOf(current + 1));
        } catch (NumberFormatException e) {
            stockQuantityField.setText("1");
        }
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
    }

    // Updates feedback label when a row is selected via info button
    @FXML
    private void selectStocks() {
        StockRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            buyFeedbackLabel.setText("No Stock Selected!");
            buyFeedbackLabel.getStyleClass().add("buy-feedback-label-none");
            buyFeedbackLabel.setTextFill(Color.RED);
        } else {
            buyFeedbackLabel.setText(selected.companyNameProperty().get());
            buyFeedbackLabel.getStyleClass().add("buy-feedback-label-selected");
            buyFeedbackLabel.setTextFill(Color.GREEN);
        }
    }

    // Sets up the Info column with a button for each row to show detailed info
    private void setupInfoColumn() {
        infoCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            {
                ImageView iv = new ImageView(new Image(
                        Objects.requireNonNull(getClass()
                                .getResourceAsStream("/com/javarepowizards/portfoliomanager/images/StockInfo64x64.png"))
                ));
                iv.setFitWidth(32); iv.setFitHeight(32);
                btn.setGraphic(iv);
                btn.getStyleClass().add("image-button");
                btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                btn.setTooltip(new Tooltip("Check this stock out"));
                btn.setOnAction(evt -> {
                    StockRow data = getTableView().getItems().get(getIndex());
                    tableView.getSelectionModel().select(data);
                    selectStocks();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // Sets up the Favourite column with toggle buttons for each row
    private void setupFavouriteColumn() {
        favouriteCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            private final ImageView favOn = new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/Favourited32x32.png")
                    ))
            );
            private final ImageView favOff = new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream("/com/javarepowizards/portfoliomanager/images/Unfavourited32x32.png")
                    ))
            );
            {
                favOn.setFitWidth(32); favOn.setFitHeight(32);
                favOff.setFitWidth(32); favOff.setFitHeight(32);
                btn.getStyleClass().add("image-button");
                btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                btn.setOnAction(evt -> {
                    StockRow row = tableView.getItems().get(getIndex());
                    StockName name = StockName.fromString(row.tickerProperty().get());
                    try {
                        List<StockName> list = watchlistDAO.listForUser(currentUserId);
                        if (list.contains(name)) {
                            watchlistDAO.removeForUser(currentUserId, name);
                            btn.setGraphic(favOff);
                        } else {
                            watchlistDAO.addForUser(currentUserId, name);
                            btn.setGraphic(favOn);
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                StockName name = StockName.fromString(
                        getTableView().getItems().get(getIndex()).tickerProperty().get()
                );
                try {
                    boolean isFav = watchlistDAO.listForUser(currentUserId).contains(name);
                    btn.setGraphic(isFav ? favOn : favOff);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                setGraphic(btn);
            }
        });
    }

    // Adds live-search capability to filter stocks by ticker or name
    private void setupSearchFunctionality() {
        stockSearchField.textProperty().addListener((obs, oldV, newV) -> {
            String filter = newV.trim().toLowerCase();
            if (filter.isEmpty()) {
                tableView.setItems(FXCollections.observableArrayList(allStocks));
                return;
            }
            ObservableList<StockRow> filtered = FXCollections.observableArrayList();
            for (StockRow row : allStocks) {
                if (row.tickerProperty().get().toLowerCase().contains(filter)
                        || row.companyNameProperty().get().toLowerCase().contains(filter)) {
                    filtered.add(row);
                }
            }
            tableView.setItems(filtered);
        });
    }
}
