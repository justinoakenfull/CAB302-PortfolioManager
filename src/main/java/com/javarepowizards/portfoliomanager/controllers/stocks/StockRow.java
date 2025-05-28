package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import javafx.beans.property.*;

/**
 * UI data model representing a row in the stock table.
 * Extracts and wraps properties from an IStock object and its latest PriceRecord.
 * Provides observable properties for JavaFX bindings in the TableView.
 */
public class StockRow {

    /** Stock ticker symbol */
    private final StringProperty ticker = new SimpleStringProperty();

    /** Company name */
    private final StringProperty companyName = new SimpleStringProperty();

    /** Opening price */
    private final DoubleProperty open = new SimpleDoubleProperty();

    /** Closing price */
    private final DoubleProperty close = new SimpleDoubleProperty();

    /** Absolute price change (close - open) */
    private final DoubleProperty change = new SimpleDoubleProperty();

    /** Percentage change in price */
    private final DoubleProperty changePercent = new SimpleDoubleProperty();

    /** Trading volume */
    private final LongProperty volume = new SimpleLongProperty();

    /**
     * Constructs a StockRow using a given IStock instance.
     *
     * @param stock Stock data source
     */
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

    /** @return Observable ticker property */
    public StringProperty tickerProperty() { return ticker; }

    /** @return Observable company name property */
    public StringProperty companyNameProperty() { return companyName; }

    /** @return Observable open price property */
    public DoubleProperty openProperty() { return open; }

    /** @return Observable close price property */
    public DoubleProperty closeProperty() { return close; }

    /** @return Observable absolute change property */
    public DoubleProperty changeProperty() { return change; }

    /** @return Observable percentage change property */
    public DoubleProperty changePercentProperty() { return changePercent; }

    /** @return Observable trading volume property */
    public LongProperty volumeProperty() { return volume; }
}
