package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import javafx.beans.property.*;
import javafx.scene.control.Button;

/**
 * A row in the watchlist table, backed by an IStock instance.
 */
public class WatchlistRow {
    private final StringProperty shortName     = new SimpleStringProperty();
    private final StringProperty displayName   = new SimpleStringProperty();
    private final DoubleProperty open          = new SimpleDoubleProperty();
    private final DoubleProperty close         = new SimpleDoubleProperty();
    private final DoubleProperty change        = new SimpleDoubleProperty();
    private final DoubleProperty changePercent = new SimpleDoubleProperty();
    private final DoubleProperty price         = new SimpleDoubleProperty();
    private final LongProperty   volume        = new SimpleLongProperty();
    private final ObjectProperty<Button> remove = new SimpleObjectProperty<>();

    /**
     * Constructs a WatchlistRow from a stock and a remove callback.
     * @param stock the IStock to display
     * @param onRemove callback invoked when remove button is clicked
     */
    public WatchlistRow(IStock stock, Runnable onRemove) {
        PriceRecord rec = stock.getCurrentRecord();

        shortName.set(stock.getTicker());
        displayName.set(stock.getCompanyName());

        open.set(rec.getOpen());
        close.set(rec.getClose());
        change.set(rec.getClose() - rec.getOpen());
        changePercent.set(open.get() == 0
                ? 0
                : ((change.get() / open.get()) * 100));

        // price can represent last traded price or close
        price.set(rec.getClose());
        volume.set(rec.getVolume());

        Button btn = new Button("Remove");
        btn.getStyleClass().add("btn-danger");
        btn.setOnAction(e -> onRemove.run());
        btn.setStyle("-fx-background-color:#c0392b; -fx-text-fill: white");
        remove.set(btn);
    }

    /**
     * Returns the property representing the stock’s short name.
     *
     * @return the StringProperty for the short name
     */
    public StringProperty shortNameProperty()     { return shortName; }

    /**
     * Returns the property representing the stock’s full display name.
     *
     * @return the StringProperty for the display name
     */
    public StringProperty displayNameProperty()   { return displayName; }

    /**
     * Returns the property representing the opening price.
     *
     * @return the DoubleProperty for the opening price
     */
    public DoubleProperty openProperty()          { return open; }

    /**
     * Returns the property representing the closing price.
     *
     * @return the DoubleProperty for the closing price
     */
    public DoubleProperty closeProperty()         { return close; }

    /**
     * Returns the property representing the absolute price change.
     *
     * @return the DoubleProperty for the price change
     */
    public DoubleProperty changeProperty()        { return change; }

    /**
     * Returns the property representing the percentage price change.
     *
     * @return the DoubleProperty for the percentage change
     */
    public DoubleProperty changePercentProperty() { return changePercent; }


    /**
     * Returns the property representing the current price.
     *
     * @return the DoubleProperty for the current price
     */
    public DoubleProperty priceProperty()         { return price; }

    /**
     * Returns the property representing the trading volume.
     *
     * @return the LongProperty for the volume
     */
    public LongProperty volumeProperty()          { return volume; }

    /**
     * Returns the property representing the remove button control.
     *
     * @return the ObjectProperty containing the remove Button
     */
    public ObjectProperty<Button> removeProperty(){ return remove; }
}
