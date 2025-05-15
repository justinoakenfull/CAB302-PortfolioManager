package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


/**
 * Controller for the watchlist modal dialog.
 * Responsible for populating and displaying detailed stock information
 * in a separate modal window.
 */
public class WatchlistModalController {

    @FXML private Label    stockTitle;
    @FXML private Label    openLabel, closeLabel, changeLabel, changePercentLabel, volumeLabel;
    @FXML private TextArea longDescArea;

    /**
     * Populate the modal view with data from the given stock.
     * Sets the company name, ticker, price details (open, close, change, change percent),
     * trading volume, and long description on the corresponding UI controls.
     *
     * @param stock the stock whose data will be displayed in the modal
     */
    public void initData(IStock stock) {
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

        longDescArea.setText(stock.getLongDescription());
    }

    @FXML
    private void onClose() {
        // close the modal Stage
        Stage stage = (Stage) stockTitle.getScene().getWindow();
        stage.close();
    }
}
