package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.MainController;
import com.javarepowizards.portfoliomanager.dao.UserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @Autowired
    private AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        // Get values from the fields
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username/email and password.");
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            Optional<User> userOpt = userDAO.getUserByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (authService.verifyPassword(password, user.getPasswordHash())) {
                    loadDashboard();
                } else {
                    showAlert("Login Failed", "Invalid email or password");
                }
            } else {
                showAlert("Login Failed", "User not found");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not verify credentials: " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

