package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import javafx.beans.property.*;

public class StockRow {
    private final StringProperty ticker = new SimpleStringProperty();
    private final StringProperty companyName = new SimpleStringProperty();
    private final DoubleProperty open = new SimpleDoubleProperty();
    private final DoubleProperty close = new SimpleDoubleProperty();
    private final DoubleProperty change = new SimpleDoubleProperty();
    private final DoubleProperty changePercent = new SimpleDoubleProperty();
    private final LongProperty volume = new SimpleLongProperty();

    public StockRow(IStock stock) {
        PriceRecord rec = stock.getCurrentRecord();

        ticker.set(stock.getTicker());
        companyName.set(stock.getCompanyName());
        open.set(rec.getOpen());
        close.set(rec.getClose());

        double diff = rec.getClose() - rec.getOpen();
        change.set(diff);
        changePercent.set(rec.getOpen() == 0 ? 0 : (diff / rec.getOpen()) * 100);
        volume.set(rec.getVolume());
    }

    public StringProperty tickerProperty() { return ticker; }
    public StringProperty companyNameProperty() { return companyName; }
    public DoubleProperty openProperty() { return open; }
    public DoubleProperty closeProperty() { return close; }
    public DoubleProperty changeProperty() { return change; }
    public DoubleProperty changePercentProperty() { return changePercent; }
    public LongProperty volumeProperty() { return volume; }
}