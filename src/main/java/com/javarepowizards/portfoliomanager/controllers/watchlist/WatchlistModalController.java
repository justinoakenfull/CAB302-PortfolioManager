package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


/**
 * Controller for the watchlist modal dialog.
 * Responsible for populating and displaying detailed stock information
 * in a separate modal window.
 */
public class WatchlistModalController implements Initializable {

    @FXML private Label    stockTitle;
    @FXML private Label    openLabel, closeLabel, changeLabel, changePercentLabel, volumeLabel;
    @FXML private TextArea longDescArea;

    private IWatchlistService watchlistService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.watchlistService = AppContext.getService(IWatchlistService.class);
    }

    /**
     * Populate the modal view with data from the given stock.
     * Sets the company name, ticker, price details (open, close, change, change percent),
     * trading volume, and long description on the corresponding UI controls.
     *
     * @param symbol the StockName whose details should be shown
     * @throws IOException   if loading the stock fails
     * @throws SQLException  if any persistence error occurs (e.g. user lookup)
     */
    public void initData(StockName symbol) throws IOException, SQLException {

        IStock stock = watchlistService.getStock(symbol);
        stockTitle.setText(stock.getCompanyName() + " (" + stock.getTicker() + ")");
        PriceRecord rec = stock.getCurrentRecord();

        openLabel.setText(String.format("%.2f", rec.getOpen()));
        openLabel.getStyleClass().add("balance-text");

        closeLabel.setText(String.format("%.2f", rec.getClose()));
        closeLabel.getStyleClass().add("balance-text");

        double change = rec.getClose() - rec.getOpen();
        changeLabel.setText(String.format("%.2f", change));
        String changeStyle = change > 0
                ? "balance-value-positive"
                : change < 0
                ? "balance-value-negative"
                : "balance-value-neutral";
        changeLabel.getStyleClass().add(changeStyle);

        double pct = rec.getOpen() == 0
                ? 0
                : (change / rec.getOpen()) * 100;
        changePercentLabel.setText(String.format("%.2f%%", pct));
        String pctStyle = pct > 0
                ? "balance-value-positive"
                : pct < 0
                ? "balance-value-negative"
                : "balance-value-neutral";
        changePercentLabel.getStyleClass().add(pctStyle);

        volumeLabel.setText(String.valueOf(rec.getVolume()));
        volumeLabel.getStyleClass().add("balance-text");

        longDescArea.setText("Loading full description…");

        Thread t = getAIThread(symbol);
        t.setDaemon(true);
        t.start();
    }

    private Thread getAIThread(StockName symbol) {
        Task<String> longDescTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return watchlistService.getLongDescription(symbol);
            }
        };

        longDescTask.setOnSucceeded(e -> longDescArea.setText(longDescTask.getValue()));
        longDescTask.setOnFailed(e   -> longDescArea.setText("Failed to load full description"));

        return new Thread(longDescTask);
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) stockTitle.getScene().getWindow();
        stage.close();
    }
}
