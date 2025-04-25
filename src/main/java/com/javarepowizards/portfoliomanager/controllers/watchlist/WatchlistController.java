package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.WatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

public class WatchlistController implements Initializable {

    @FXML private TableView<WatchlistRow> tableView;
    @FXML private TableColumn<WatchlistRow, String> shortStockColumn;
    @FXML private TableColumn<WatchlistRow, String> stockColumn;
    @FXML private TableColumn<WatchlistRow, Double> openColumn;
    @FXML private TableColumn<WatchlistRow, Double> closeColumn;
    @FXML private TableColumn<WatchlistRow, Double> changeColumn;
    @FXML private TableColumn<WatchlistRow, Double> changePercentColumn;
    @FXML private TableColumn<WatchlistRow, Double> priceColumn;
    @FXML private TableColumn<WatchlistRow, Long>   volumeColumn;
    @FXML private TableColumn<WatchlistRow, Button> removeColumn;
    @FXML private Button addStockButton;

    private WatchlistDAO watchlistDAO;
    private StockRepository repo;
    private int currentUserId = 1; // TODO: set this on login

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs and repository
        try {
            watchlistDAO = new WatchlistDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init WatchlistDAO", e);
        }
        repo = AppContext.getService(StockRepository.class);

        // 1) Wire up cell value factories
        shortStockColumn.setCellValueFactory(c -> c.getValue().shortNameProperty());
        stockColumn.setCellValueFactory(c -> c.getValue().displayNameProperty());
        openColumn.setCellValueFactory(c -> c.getValue().openProperty().asObject());
        closeColumn.setCellValueFactory(c -> c.getValue().closeProperty().asObject());
        changeColumn.setCellValueFactory(c -> c.getValue().changeProperty().asObject());
        changePercentColumn.setCellValueFactory(c -> c.getValue().changePercentProperty().asObject());
        priceColumn.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        volumeColumn.setCellValueFactory(c -> c.getValue().volumeProperty().asObject());
        removeColumn.setCellValueFactory(c -> c.getValue().removeProperty());

        HBox.setHgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 2) Custom cell formatting
        configureCellFactories();

        // 3) Load initial rows
        refreshTable();
    }

    private void configureCellFactories() {
        // change: red/green
        changeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", v));
                    setStyle(v >= 0
                            ? "-fx-text-fill: green; -fx-alignment: CENTER;"
                            : "-fx-text-fill: red;   -fx-alignment: CENTER;");
                }
            }
        });

        // change%: fmt + sign
        changePercentColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%+2.2f%%", v));
                    setStyle(v >= 0
                            ? "-fx-text-fill: green; -fx-alignment: CENTER;"
                            : "-fx-text-fill: red;   -fx-alignment: CENTER;");
                }
            }
        });

        // price: AUD currency
        priceColumn.setCellFactory(col -> new TableCell<>() {
            private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en","AU"));
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText((empty || v == null) ? null : fmt.format(v));
            }
        });

        // volume: thousands separator
        volumeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Long v, boolean empty) {
                super.updateItem(v, empty);
                setText((empty || v == null) ? null : String.format("%,d", v));
            }
        });

        // remove button: handled in WatchlistRow constructor
    }

    private void refreshTable() {
        try {
            List<StockName> symbols = watchlistDAO.listForUser(currentUserId);
            ObservableList<WatchlistRow> rows = FXCollections.observableArrayList();

            StockRepository repo = AppContext.getStockRepository();
            Set<String> available = repo.availableTickers();

            for (StockName sym : symbols) {
                String ticker = sym.getSymbol();    // <-- use getSymbol(), not sym.name()
                if (!available.contains(ticker)) {
                    System.err.println("No CSV history for " + ticker + ", skipping");
                    continue;
                }
                IStock stock = repo.getByTicker(ticker);
                rows.add(new WatchlistRow(stock, () -> {
                    try {
                        watchlistDAO.removeForUser(currentUserId, sym);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    refreshTable();
                }));
            }
            tableView.setItems(rows);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    private void onAddStock() {
        ChoiceDialog<StockName> dlg = new ChoiceDialog<>(StockName.values()[0], List.of(StockName.values()));
        dlg.setTitle("Add to Watchlist");
        dlg.setHeaderText("Select a stock to watch");
        dlg.setContentText("Symbol:");
        dlg.showAndWait().ifPresent(sym -> {
            try {
                watchlistDAO.addForUser(currentUserId, sym);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            refreshTable();
        });
    }
}
