package com.javarepowizards.portfoliomanager.controllers.dashboard;

import com.javarepowizards.portfoliomanager.ui.QuickTips;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class DashboardController {

    @FXML
    private Label watchListLabel;

    @FXML
    private  Label quickTipsLabel;

    @FXML
    public void initialize() {
        QuickTips quickTips= new QuickTips(quickTipsLabel);
        quickTips.start();
    }
}


