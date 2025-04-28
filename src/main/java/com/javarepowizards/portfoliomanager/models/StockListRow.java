package com.javarepowizards.portfoliomanager.models;

import com.javarepowizards.portfoliomanager.models.StockData;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

public class StockListRow extends HBox{
    protected final Label symbolLabel = new Label();
    protected final Label priceLabel = new Label();
    protected final Label highLabel = new Label();
    protected final Label lowLabel = new Label();
    protected final Label closeLabel = new Label();

    protected final StockData stockData;

    public StockListRow(StockData stockData) {
        this.stockData = stockData;

        // Setup logic and display
        symbolLabel.setText(stockData.getSymbol());
        priceLabel.setText(String.format("%.2f", stockData.getPrice()));
        highLabel.setText(String.format("H: %.2f", stockData.getHigh()));
        lowLabel.setText(String.format("L: %.2f", stockData.getLow()));
        closeLabel.setText(String.format("C: %.2f", stockData.getClose()));

        this.getChildren().addAll(symbolLabel, priceLabel, highLabel, lowLabel, closeLabel);
    }

    public StockData getStockData() {
        return stockData;
    }

}
