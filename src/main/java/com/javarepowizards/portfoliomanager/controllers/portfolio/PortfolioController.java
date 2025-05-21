package com.javarepowizards.portfoliomanager.controllers.portfolio;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.services.Session;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;

import java.util.List;

public class PortfolioController {

    //FXML-injected controls
    @FXML private PieChart pieChart;               // the donut showing holdings distribution
    @FXML private Text totalValueText;             // displays total portfolio value
    @FXML private Text changePctText;              // placeholder for overall change %
    @FXML private TableView<PortfolioEntry> portfolioTable;              // bottom table
    @FXML private TableColumn<PortfolioEntry,String> stockCol;          // stock name column
    @FXML private TableColumn<PortfolioEntry,String> changeCol;         // % of portfolio column
    @FXML private TableColumn<PortfolioEntry,Number> balanceCol;        // $ value column

    // grab the user DAO and current user’s ID from our AppContext/session
    private final IPortfolioDAO portfolioDAO = AppContext.getService(IPortfolioDAO.class);
    private final int currentUserId = Session.getCurrentUser().getUserId();

    @FXML
    public void initialize() {
        setupTableColumns();   // wire up columns (ticker → name, %, $)
        refreshPortfolio();    // fetch from DB and render pie + table
    }

    /**
     * Configures each TableColumn’s cell factory.
     */
    private void setupTableColumns() {
        // show stock display name (e.g. "BHP Group Ltd")
        stockCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStock().getDisplayName())
        );

        // compute each holding's percentage of the total portfolio
        changeCol.setCellValueFactory(c -> {
            PortfolioEntry entry = c.getValue();

            // sum up marketValue for all entries in this table
            double total = c.getTableView()
                    .getItems()
                    .stream()
                    .mapToDouble(PortfolioEntry::getMarketValue)
                    .sum();

            // avoid division-by-zero
            double pctOfTotal = total > 0
                    ? entry.getMarketValue() / total * 100
                    : 0.0;

            return new SimpleStringProperty(String.format("%.2f%%", pctOfTotal));
        });

        // show each holding’s market value formatted as currency
        balanceCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getMarketValue())
        );
        balanceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", val.doubleValue()));
                }
            }
        });
    }

    /**
     * Fetches the user’s holdings from the DB, rebuilds the pie chart & table.
     */
    private void refreshPortfolio() {
        List<PortfolioEntry> holdings;
        try {
            holdings = portfolioDAO.getHoldingsForUser(currentUserId);
        } catch (Exception e) {
            e.printStackTrace();
            return;  // bail out on error
        }

        // rebuild pie slices by actual market value
        var data = pieChart.getData();
        data.clear();
        for (PortfolioEntry entry : holdings) {
            data.add(new PieChart.Data(
                    entry.getStock().getDisplayName(),
                    entry.getMarketValue()
            ));
        }

        // after nodes are created, attach tooltips + hover highlights + scaling
        Platform.runLater(() -> {
            for (PieChart.Data slice : data) {
                Node node = slice.getNode();
                node.setPickOnBounds(true);  // entire slice is clickable

                // show exact dollar value on hover
                Tooltip.install(node, new Tooltip(
                        slice.getName() + ": " + String.format("$%.2f", slice.getPieValue())
                ));

                //  glow effect on hover
                node.setOnMouseEntered(e -> {
                    node.setScaleX(1.1);
                    node.setScaleY(1.1);
                });
                node.setOnMouseExited(e -> {
                    node.setScaleX(1.0);
                    node.setScaleY(1.0);
                });
            }

            // scale the whole chart +10% per stock, max +50%
            double factor = 1.0 + Math.min(holdings.size(), 5) * 0.1;
            pieChart.setScaleX(factor);
            pieChart.setScaleY(factor);
        });

        // update summary texts
        double total = holdings.stream()
                .mapToDouble(PortfolioEntry::getMarketValue)
                .sum();
        totalValueText.setText(String.format("$%.2f", total));
        changePctText.setText("0.00%");  // could calculate real change here

        //populate bottom table
        portfolioTable.setItems(FXCollections.observableArrayList(holdings));
    }
}
