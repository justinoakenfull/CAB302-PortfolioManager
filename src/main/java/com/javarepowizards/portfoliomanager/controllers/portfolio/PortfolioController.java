package com.javarepowizards.portfoliomanager.controllers.portfolio;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.services.Session;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.text.Text;

import java.util.List;

public class PortfolioController {

    @FXML private PieChart pieChart;
    @FXML private Text totalValueText;
    @FXML private Text changePctText;

    // pull the DAO by its interface (registered in MainApplication)
    private final IUserDAO userDAO = AppContext.getService(IUserDAO.class);
    private final int currentUserId = Session.getCurrentUser().getUserId();

    @FXML
    public void initialize() {
        refreshPortfolio();
    }

    private void refreshPortfolio() {
        List<PortfolioEntry> holdings;
        try {
            // load exactly what the user holds
            holdings = userDAO.getHoldingsForUser(currentUserId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // rebuild the pie slices
        var data = pieChart.getData();
        data.clear();
        for (PortfolioEntry e : holdings) {
            data.add(new PieChart.Data(
                    e.getStock().getDisplayName(),
                    e.getAmountHeld()
            ));
        }

        // compute total current market value
        double total = holdings.stream()
                .mapToDouble(PortfolioEntry::getMarketValue)
                .sum();
        totalValueText.setText(String.format("$%.2f", total));

        // (optionally) compute % change vs. a baseline—here we leave it zero
        changePctText.setText("0.00%");
    }
}
