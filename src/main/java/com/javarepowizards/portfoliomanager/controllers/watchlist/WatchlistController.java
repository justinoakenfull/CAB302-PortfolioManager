package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;


import com.javarepowizards.portfoliomanager.models.StockData;
//import com.javarepowizards.portfoliomanager.ui.StockListRow;

import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;

import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;



public class WatchlistController implements Initializable {

    @FXML private VBox stockListContainer;


    private IWatchlistDAO watchlistDAO;
    private StockRepository repo;
    private final int currentUserId = 1;

  //  public DropShadow dropShadow;
//    @FXML private VBox      tableContainer;
  //  @FXML private TableView<WatchlistRow> tableView;
    @FXML private Button    addStockButton;
    @FXML private VBox getStockListContainer;

   // @FXML private ListView<IStock> watchlistListView;
 //   private final ObservableList<IStock> watchlistObservableList = FXCollections.observableArrayList();

//    private IWatchlistDAO watchlistDAO;
 //   private StockRepository repo;
 //   private int currentUserId = 1; // TODO: set this on login

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        watchlistDAO = AppContext.getService(IWatchlistDAO.class);
        repo = AppContext.getService(StockRepository.class);
        refreshStockList();
    }


      //  try {
    //        refreshTable();
    //        refreshStockList();
    //    } catch (IOException | SQLException e) {
    //        throw new RuntimeException(e);
    //    }
    //}

    private void refreshStockList() {

        stockListContainer.getChildren().clear();

        List<StockName> symbols;
        try {
            symbols = watchlistDAO.listForUser(currentUserId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (StockName sym : symbols) {
            IStock stock;
            try {
                stock = repo.getByTicker(sym.getSymbol());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (stock == null) continue;

            Runnable onRemove = () -> {
                try {
                    watchlistDAO.removeForUser(currentUserId, sym);
                    refreshStockList();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            };

            stockListContainer.getChildren().add(new WatchlistRow(stock, onRemove));

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
                refreshStockList();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

}

  /*  private void refreshTable() throws IOException, SQLException {

        List<StockName> symbols = watchlistDAO.listForUser(currentUserId);
        List<WatchlistRow> rows = new ArrayList<>();

        Set<String> available = repo.availableTickers();

        for (StockName sym : symbols) {
            String ticker = sym.getSymbol();
            if (!available.contains(ticker)) {
                System.err.println("No CSV history for " + ticker + ", skipping");
                continue;
            }
            IStock stock = repo.getByTicker(ticker);
            rows.add(new WatchlistRow(stock, () -> {
                try {
                    watchlistDAO.removeForUser(currentUserId, sym);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    refreshTable();
                } catch (IOException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        ObservableList<WatchlistRow> model = FXCollections.observableArrayList(rows);
        // 2) describe columns
        List<ColumnConfig<WatchlistRow,?>> cols = List.of(
                new ColumnConfig<>("Ticker",
                        WatchlistRow::shortNameProperty),
                new ColumnConfig<>("Name",
                        WatchlistRow::displayNameProperty),
                new ColumnConfig<>("Open",
                        r -> r.openProperty().asObject(),
                        TableCellFactories.numericFactory(2,false)),
                new ColumnConfig<>("Close",
                        r -> r.closeProperty().asObject(),
                        TableCellFactories.numericFactory(2,false)),
                new ColumnConfig<>("Change",
                        r -> r.changeProperty().asObject(),
                        TableCellFactories.numericFactory(2,true)),
                new ColumnConfig<>("Change %",
                        r -> r.changePercentProperty().asObject(),
                        TableCellFactories.numericFactory(2,true)),
                new ColumnConfig<>("Price",
                        r -> r.priceProperty().asObject(),
                        TableCellFactories.currencyFactory(new Locale("en","AU"), 2)),
                new ColumnConfig<>("Volume",
                        r -> r.volumeProperty().asObject(),
                        TableCellFactories.longFactory()),
                new ColumnConfig<>("Remove",
                        WatchlistRow::removeProperty)
        );


        TableView<WatchlistRow> table = TableViewFactory.create(cols);

        // 1) copy over the style class from FXML
        table.getStyleClass().add("watchlist-table");

        // 2) re-apply the drop shadow (if you want it)
        table.setEffect(tableView.getEffect());

        // 3) preserve the layout constraints
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        // 4) swap it in
        tableContainer.getChildren().setAll(table);
        tableView = table;
        table.setItems(model);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().forEach(col ->
                col.getStyleClass().add("column-header-background")
        );

    }

    private void refreshStockList() throws IOException, SQLException{
        stockListContainer.getChildren().clear();

        List<StockName> symbols = watchlistDAO.listForUser(currentUserId);

        Set<String> available = repo.availableTickers();

        for(StockName sym : symbols){
            String ticker = sym.getSymbol();
            if (!available.contains(ticker)) continue;

            IStock stock = repo.getByTicker(ticker);
            if (stock == null) continue;

            WatchlistRow row = new WatchlistRow(stock, () -> {
                watchlistDAO.removeForUser((currentUserId,sym);
                refreshStockList();

            });

                stockListContainer.getChildren().add(row);
            //{
          //      System.err.println("No CVS history for" + ticker + ", skipping");
          //  }

      //      List<StockData> stockDataList = AppContext.getService(StockDAO.class).getStockData(sym);

        //    if (stockDataList.isEmpty()){
         //       System.err.println("No stock data for " + ticker);
        //        continue;
          //  }

       //     StockData latest = stockDataList.get(stockDataList.size() -1);

       //     StockListRow row = new StockListRow(latest);
        //    stockListContainer.getChildren().add(row);
        }
    }
*/






