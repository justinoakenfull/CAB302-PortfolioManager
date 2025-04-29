package com.javarepowizards.portfoliomanager.controllers.dashboard;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.models.StockListRow;
import com.javarepowizards.portfoliomanager.models.Watchlist;
import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private VBox watchlistPreviewContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Grab the user’s watchlist (populated earlier in AppContext)
        Watchlist watchlist = AppContext.getService(Watchlist.class);

        // For each WatchlistRow, pull out its IStock and wrap in a preview row
        for (WatchlistRow watchlistRow : watchlist.getRows()) {
            IStock stock = watchlistRow.getStock();
            StockListRow previewRow = new StockListRow(stock);
            watchlistPreviewContainer.getChildren().add(previewRow);
        }
    }
}


   //     Set<String> availableTickers = repo.availableTickers();

  //      int count = 0;

       // for (String ticker : availableTickers) {
      //      if (count >= 3) break;

      //      IStock stock = repo.getByTicker(ticker);
      //      if (stock == null) continue;

        //    PriceRecord price = stock.getCurrentRecord();
        //    if (price == null) continue;

        //    StockData data = new StockData(LocalDate.now(), ticker);
        //    data.setPrice(price.getClose());
         //   data.setHigh(price.getHigh());
         //   data.setLow(price.getLow());
         //   data.setClose(price.getClose());

         //   StockListRow row = new StockListRow(data);
         //   watchlistPreviewContainer.getChildren().add(row);

         //   count++;
      //  }



