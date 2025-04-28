package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;


import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class StockListTableController implements Initializable {

    @FXML private TableView<StockData> tableView;
    @FXML private TableColumn<StockData, String> symbolCol;
    @FXML private TableColumn<StockData, Double> openCol;
    @FXML private TableColumn<StockData, Double> closeCol;
    @FXML private TableColumn<StockData, Double> changeCol;
    @FXML private TableColumn<StockData, Double> changePercentCol;
    @FXML private TableColumn<StockData, Double> priceCol;

    private final StockDAO stockDAO = new StockDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources){
        symbolCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getStockName().getSymbol()
                ));
        openCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getClose()).asObject()
        );
        closeCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getClose()).asObject()
        );

        changeCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getPrice()).asObject()
        );

        changePercentCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getPrice()).asObject()
        );

        priceCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getPrice()).asObject()
        );

        var data = FXCollections.observableArrayList(
                stockDAO.getStockData(StockName.WES_AX)
        );
        tableView.setItems(data);



    }
}
