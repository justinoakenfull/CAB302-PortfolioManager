package com.javarepowizards.portfoliomanager.controllers.portfolio;

import com.javarepowizards.portfoliomanager.models.StockName;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

public class PortfolioController {
    @FXML
    private PieChart pieChart;

    @FXML
    public void initialize() {
        pieChart.getData().setAll(
                new PieChart.Data(StockName.AMC_AX.getDisplayName(), 300),
                new PieChart.Data(StockName.TLS_AX.getDisplayName(), 700),
                new PieChart.Data(StockName.BXB_AX.getDisplayName(), 200),
                new PieChart.Data(StockName.RIO_AX.getDisplayName(), 100)
        );
    }

}
