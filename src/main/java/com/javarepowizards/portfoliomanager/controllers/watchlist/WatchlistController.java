package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.MainApplication;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.dao.WatchlistDAO;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
    private StockDAO stockDAO;
    private int currentUserId = 1; // TODO: set this when user logs in

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            watchlistDAO = new WatchlistDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init WatchlistDAO", e);
        }
        stockDAO = new StockDAO();
        try {
            URL url = MainApplication.class.getResource("/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");
            String csvFilePath = url.getFile();
            stockDAO.loadCSV(csvFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1) Wire up value factories
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
                setText((empty || v==null) ? null : fmt.format(v));
            }
        });

        // volume: thousands separator
        volumeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Long v, boolean empty) {
                super.updateItem(v, empty);
                setText((empty || v==null) ? null : String.format("%,d", v));
            }
        });

        // reomve button: deletes both from DB & table
        removeColumn.setCellFactory(new Callback<>() {
            @Override public TableCell<WatchlistRow, Button> call(TableColumn<WatchlistRow, Button> col) {
                return new TableCell<>() {
                    final Button btn = new Button("Remove");
                    {
                        btn.getStyleClass().add("btn-danger");
                        btn.setOnAction(e -> {
                            WatchlistRow row = getTableView().getItems().get(getIndex());
                            try {
                                watchlistDAO.removeForUser(currentUserId, StockName.valueOf(row.shortNameProperty().get()));
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            refreshTable();
                        });
                    }
                    @Override protected void updateItem(Button item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
    }

    private void refreshTable() {
        try {
            List<StockName> symbols = watchlistDAO.listForUser(currentUserId);
            ObservableList<WatchlistRow> rows = FXCollections.observableArrayList();
            for (StockName sym : symbols) {
                // pick latest by date
                StockData latest = stockDAO.getStockData(sym).stream()
                        .max(Comparator.comparing(StockData::getDate))
                        .orElse(new StockData(LocalDate.now()));
                rows.add(new WatchlistRow(sym, latest, new Button())); // remove btn is built in cellFactory
            }
            tableView.setItems(rows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddStock() {
        StockName defaultSymbol = StockName.values()[0];
        ChoiceDialog<StockName> dlg = new ChoiceDialog<>(defaultSymbol,
                List.of(StockName.values()));
        dlg.setTitle("Add to Watchlist");
        dlg.setHeaderText("Select a stock to watch");
        dlg.setContentText("Symbol:");

        DialogPane pane = dlg.getDialogPane();
        String css = getClass()
                .getResource(
                        "/com/javarepowizards/portfoliomanager/views/watchlist/watchlist.css"
                )
                .toExternalForm();

        pane.getStylesheets().add(css);
        pane.getStyleClass().add("dialog-pane");

        dlg.showAndWait()
                .ifPresent(sym -> {
                    try {
                        watchlistDAO.addForUser(currentUserId, sym);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    refreshTable();
                });
    }
}
