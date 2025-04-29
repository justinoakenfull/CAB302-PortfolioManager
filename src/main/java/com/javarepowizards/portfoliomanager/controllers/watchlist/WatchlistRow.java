package com.javarepowizards.portfoliomanager.controllers.watchlist;

// import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
// import javafx.beans.property.*;
import com.javarepowizards.portfoliomanager.models.StockListRow;
import javafx.scene.control.Button;

/**
 * A row in the watchlist table, backed by an IStock instance.
 */
public class WatchlistRow extends StockListRow {
    private final Button removeButton = new Button("Remove");

    public WatchlistRow(IStock stock, Runnable onRemove) {
        super(stock);
        removeButton.setOnAction(e -> onRemove.run());
        getChildren().add(removeButton); // add the button to the row
    }
}


     //   PriceRecord rec = stock.getCurrentRecord();

     //   shortName.set(stock.getTicker());
      //  displayName.set(stock.getCompanyName());

   //     open.set(rec.getOpen());
  //      close.set(rec.getClose());
    //    change.set(rec.getClose() - rec.getOpen());
   //     changePercent.set(open.get() == 0
    //            ? 0
     //           : ((change.get() / open.get()) * 100));

        // price can represent last traded price or close
    //    price.set(rec.getClose());
   //     volume.set(rec.getVolume());

    //    Button btn = new Button("Remove");
    //    btn.getStyleClass().add("btn-danger");
    //    btn.setOnAction(e -> onRemove.run());
    //    remove.set(btn);

   // public StringProperty shortNameProperty()     { return shortName; }
 //   public StringProperty displayNameProperty()   { return displayName; }
 //   public DoubleProperty openProperty()          { return open; }
  //  public DoubleProperty closeProperty()         { return close; }
  //  public DoubleProperty changeProperty()        { return change; }
 //   public DoubleProperty changePercentProperty() { return changePercent; }
  //  public DoubleProperty priceProperty()         { return price; }
 //   public LongProperty volumeProperty()          { return volume; }
   // public ObjectProperty<Button> removeProperty(){ return remove; }

