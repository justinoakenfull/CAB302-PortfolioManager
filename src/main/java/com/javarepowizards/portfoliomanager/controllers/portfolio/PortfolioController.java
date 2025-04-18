package com.javarepowizards.portfoliomanager.controllers.portfolio;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.util.ResourceBundle;

// Controller for Portfolio view: table + pie chart
public class PortfolioController implements Initializable {

    // UI elements from FXML
    @FXML private TextField searchField;
    @FXML private TableView<PortfolioItem> portfolioTable;
    @FXML private TableColumn<PortfolioItem, String> stockColumn;
    @FXML private TableColumn<PortfolioItem, Double> changeColumn;
    @FXML private TableColumn<PortfolioItem, Integer> balanceColumn;
    @FXML private PieChart portfolioPieChart;

    // Data lists
    private final ObservableList<PortfolioItem> masterData = FXCollections.observableArrayList();
    private FilteredList<PortfolioItem> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPortfolioData();
        initFiltering();
        initSorting();
        updatePieChart();
    }

    // Set up how each table column gets its data
    private void setupTableColumns() {
        stockColumn.setCellValueFactory(c -> c.getValue().symbolProperty());
        changeColumn.setCellValueFactory(c -> c.getValue().changeProperty().asObject());
        balanceColumn.setCellValueFactory(c -> c.getValue().balanceProperty().asObject());
    }

    // Load data into masterData (replace with real data loading later)
    private void loadPortfolioData() {
        masterData.setAll(
                new PortfolioItem("NVDA", 1.23, 100),
                new PortfolioItem("TSLA", -0.45, 50),
                new PortfolioItem("GOOGL", 0.67, 200)
        );
    }

    // Make the searchField filter the table
    private void initFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return item.getSymbol().toLowerCase().contains(newVal.toLowerCase());
            });
        });
    }

    // Bind sorted filtered data to the table
    private void initSorting() {
        SortedList<PortfolioItem> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(portfolioTable.comparatorProperty());
        portfolioTable.setItems(sorted);
    }

    // Update pie chart slices based on balance
    private void updatePieChart() {
        portfolioPieChart.getData().clear();
        masterData.forEach(item ->
                portfolioPieChart.getData().add(new PieChart.Data(item.getSymbol(), item.getBalance()))
        );
        portfolioPieChart.setLabelsVisible(true);
        portfolioPieChart.setLabelLineLength(25);
    }

    // Simple model for each row/slice
    public static class PortfolioItem {
        private final SimpleStringProperty symbol;
        private final SimpleDoubleProperty change;
        private final SimpleIntegerProperty balance;

        public PortfolioItem(String symbol, double change, int balance) {
            this.symbol = new SimpleStringProperty(symbol);
            this.change = new SimpleDoubleProperty(change);
            this.balance = new SimpleIntegerProperty(balance);
        }

        public String getSymbol() { return symbol.get(); }
        public SimpleStringProperty symbolProperty() { return symbol; }

        public double getChange() { return change.get(); }
        public SimpleDoubleProperty changeProperty() { return change; }

        public int getBalance() { return balance.get(); }
        public SimpleIntegerProperty balanceProperty() { return balance; }
    }
}
