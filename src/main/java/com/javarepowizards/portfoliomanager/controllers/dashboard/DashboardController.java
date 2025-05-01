package com.javarepowizards.portfoliomanager.controllers.dashboard;


import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.ui.QuickTips;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.*;


/**
 * Controller class for Dashboard view.
 * Manages the quick tips section and watchlist table display
 */
public class DashboardController {

        // Label that displays rotating investment tips
        @FXML private  Label quickTipsLabel;

        // Tableview and columns for the watchlist section
        @FXML private TableColumn<WatchlistRow, Number> changeColumn;

        @FXML private TableView<WatchlistRow> watchlistTable;
        @FXML private TableColumn<WatchlistRow, String> tickerColumn;
        @FXML private TableColumn<WatchlistRow, String> nameColumn;
        @FXML private TableColumn<WatchlistRow, Number> openColumn;
        @FXML private TableColumn<WatchlistRow, Number> closeColumn;
        @FXML private TableColumn<WatchlistRow, Number> changePercentColumn;
        @FXML private TableColumn<WatchlistRow, Number> priceColumn;
        @FXML private TableColumn<WatchlistRow, Number> volumeColumn;
        @FXML private TableColumn<WatchlistRow, Button> removeColumn;


    /**
     * Called automatically when the FXML is loaded
     * Initialises quick tips and sets dynamic widths for table columns
     */
    @FXML
        public void initialize() {
            //Start rotating investment tips
            QuickTips quickTips= new QuickTips(quickTipsLabel);
            quickTips.start();

            // Bind column widths to resize proportionately with the table
            bindColumnWidths();
        }

    /**
     * Dynamically binds each table's column's width to a percentage
     * of the total table width for a responsive layout
     */
    private void bindColumnWidths() {
            tickerColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.11));
            nameColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.15));
            openColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
            closeColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
            changePercentColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
            priceColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
            volumeColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.12));
            removeColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.12));

        }
}


