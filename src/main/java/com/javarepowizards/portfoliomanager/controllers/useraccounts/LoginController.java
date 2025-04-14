package com.javarepowizards.portfoliomanager.controllers.useraccounts;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import com.javarepowizards.portfoliomanager.MainController;
import java.io.IOException;


public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;
    @FXML
    private void handleLogin() {


        // Once login is "successful," switch to the dashboard page
        loadDashboard();
    }
    private void loadDashboard() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/hello-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.showDashboard();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setWidth(800);  // or whatever width you want
            stage.setHeight(600); // or your desired height
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
