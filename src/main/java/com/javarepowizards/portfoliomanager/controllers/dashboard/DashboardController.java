package com.javarepowizards.portfoliomanager.controllers.dashboard;


import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {

    @FXML
    private TableView<StockData> watchlistTable;

    @FXML private TableColumn<StockData, String> stockColumn;
    @FXML private TableColumn<StockData, Double> openColumn;
    @FXML private TableColumn<StockData, Double> closeColumn;
    @FXML private TableColumn<StockData, Double> highColumn;
    @FXML private TableColumn<StockData, Double> lowColumn;
    @FXML private TableColumn<StockData, Double> changeColumn;

    private final StockDAO stockDAO = new StockDAO();

    @FXML
    private Label watchListLabel;

    @FXML
    public void initialize() {
        stockColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStockName().getSymbol()));

        openColumn.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getOpen()).asObject());

        closeColumn.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getClose()).asObject());

        highColumn.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getHigh()).asObject());

        highColumn.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getLow()).asObject());

        changeColumn.setCellValueFactory(cell ->{
            double delta = cell.getValue().getClose() - cell.getValue().getOpen();
            return new SimpleDoubleProperty(delta).asObject();
        });

        ObservableList<StockData> data = FXCollections.observableArrayList(
                stockDAO.getStockData(StockName.WES_AX)

        );
        watchlistTable.setItems(data);
    }
}


