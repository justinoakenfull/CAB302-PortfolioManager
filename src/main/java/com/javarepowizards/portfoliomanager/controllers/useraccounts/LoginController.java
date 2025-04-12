package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import com.javarepowizards.portfoliomanager.MainController;
import java.io.IOException;
import com.javarepowizards.portfoliomanager.services.AuthService;



public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;
    @FXML
    private void handleLogin() {
        AuthService service = new AuthService();
        String username = emailField.getText();
        String password = passwordField.getText();
        try {

            service.loginUser(username, password);

            // Successful login - navigate to dashboard view.
            loadDashboard();

        } catch (Exception e) {
            System.out.println(1);
        }


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
