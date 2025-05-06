package com.javarepowizards.portfoliomanager.controllers.stocks;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.domain.stock.Stock;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;


public class StocksController implements Initializable {
    private Stock selectedStock;

    @Autowired
    private IUserDAO userDAO;


    // private boolean IsPurchasingContracts = false; // TODO: Switch between purchasing contracts and using dollar amount.
    @FXML private TextField stockPurchaseAmount; // Can be dollar amount or contract amount.
    @FXML private TextField filterStocks;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = AppContext.getUserDAO();
    }


    // Increases the desired amount of stocks the user wants to buy.
    @FXML
    private void increasePurchaseAmount() {
        try {
            int currentValue = Integer.parseInt(stockPurchaseAmount.getText());
            stockPurchaseAmount.setText(String.valueOf(currentValue + 1));
        } catch (NumberFormatException ex) {
            stockPurchaseAmount.setText("1");
        }
    }

    // Decreases the desired amount of stocks the user wants to buy.
    @FXML
    private void decreasePurchaseAmount() {
        try {
            int currentValue = Integer.parseInt(stockPurchaseAmount.getText());
            if (currentValue > 1) { stockPurchaseAmount.setText(String.valueOf(currentValue - 1)); }
        } catch (NumberFormatException ex) {
            stockPurchaseAmount.setText("1");
        }
    }


    @FXML
    private void BuyStock() {
        // TODO: Allow user to buy stocks (will be stored in SQLite, needs UserDAO).
    }

    @FXML
    private void searchStock() {
        String filter = filterStocks.getText();
        // TODO: Parse data to StockDAO to find desired stock.
    }





}
