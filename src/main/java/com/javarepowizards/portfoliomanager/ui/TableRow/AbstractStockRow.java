package com.javarepowizards.portfoliomanager.ui.TableRow;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Base class for JavaFX table-row view-models backed by an IStock.
 * Extracts and exposes common observable properties such as ticker symbol,
 * company name, prices, changes, and volume.
 */
public abstract class AbstractStockRow {

    private final StringProperty ticker = new SimpleStringProperty();
    private final StringProperty companyName = new SimpleStringProperty();
    private final DoubleProperty open = new SimpleDoubleProperty();
    private final DoubleProperty close = new SimpleDoubleProperty();
    private final DoubleProperty change = new SimpleDoubleProperty();
    private final DoubleProperty changePercent = new SimpleDoubleProperty();
    private final LongProperty volume = new SimpleLongProperty();

    /**
     * Populates all shared properties from the given stock’s latest record.
     *
     * @param stock the IStock instance to extract data from
     */
    protected void initFrom(IStock stock) {
        PriceRecord rec = stock.getCurrentRecord();
        ticker.set(stock.getTicker());
        companyName.set(stock.getCompanyName());
        open.set(rec.open());
        close.set(rec.close());

        double diff = rec.close() - rec.open();
        change.set(diff);
        changePercent.set(rec.open() == 0
                ? 0
                : (diff / rec.open()) * 100);
        volume.set(rec.volume());
    }

    /**
     * @return observable ticker symbol property
     */
    public StringProperty tickerProperty() {
        return ticker;
    }

    /**
     * @return observable company name property
     */
    public StringProperty companyNameProperty() {
        return companyName;
    }

    /**
     * @return observable opening price property
     */
    public DoubleProperty openProperty() {
        return open;
    }

    /**
     * @return observable closing price property
     */
    public DoubleProperty closeProperty() {
        return close;
    }

    /**
     * @return observable absolute price change property
     */
    public DoubleProperty changeProperty() {
        return change;
    }

    /**
     * @return observable percentage price change property
     */
    public DoubleProperty changePercentProperty() {
        return changePercent;
    }

    /**
     * @return observable trading volume property
     */
    public LongProperty volumeProperty() {
        return volume;
    }
}
