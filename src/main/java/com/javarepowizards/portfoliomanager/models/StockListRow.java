package com.javarepowizards.portfoliomanager.models;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StockListRow extends HBox {
    protected final Label symbolLabel = new Label();
    protected final Label priceLabel = new Label();
    protected final Label highLabel = new Label();
    protected final Label lowLabel = new Label();
    protected final Label closeLabel = new Label();

    protected final IStock stock;

    public StockListRow(IStock stock) {
        this.stock = stock;
        PriceRecord rec = stock.getCurrentRecord();
        double close = rec != null ? rec.getClose() : 0;
        double high = rec != null ? rec.getHigh() : 0;
        double low = rec != null ? rec.getLow() : 0;

        setSpacing(10);
        symbolLabel.setText(stock.getTicker());
        priceLabel.setText(String.format("%.2f", close));
        highLabel.setText(String.format("H: %.2f", high));
        lowLabel.setText(String.format("L: %.2f", low));
        closeLabel.setText(String.format("C: %.2f", close));

        getChildren().addAll(symbolLabel, priceLabel, highLabel, lowLabel, closeLabel);
    }

    public IStock getStock() {
        return stock;
    }
}
