package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.MainController;
import com.javarepowizards.portfoliomanager.dao.UserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.AuthService;
import com.javarepowizards.portfoliomanager.services.Session;
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
import java.util.regex.Pattern;

@Component
public class LoginController {
    @FXML
    public Button registerButton;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private Button dummyLogin;

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
            Optional<User> userOpt = (IsEmail(emailField.getText())) ?
                    userDAO.getUserByEmail(email) : userDAO.getUserByUsername(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (authService.verifyPassword(password, user.getPasswordHash())) {
                    Session.setCurrentUser(user);
                    loadDashboard();
                } else {
                    showAlert("Login Failed", "Invalid username/email or password");
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

    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/registration.fxml"));
            Parent newRoot = loader.load();

            // Get current scene from a known node (emailField)
            Scene currentScene = emailField.getScene();
            currentScene.setRoot(newRoot);

            // Optional: update title, but window (Stage) remains the same
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Register");
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @FXML
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/hello-view.fxml"));
            Parent newRoot = loader.load();

            // Get and use current scene
            Scene currentScene = loginButton.getScene();
            currentScene.setRoot(newRoot);

            // Access controller and invoke showDashboard
            MainController mainController = loader.getController();
            mainController.showDashboard();

            // Update stage properties if needed
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Dashboard");
            stage.setWidth(1920);
            stage.setHeight(1080);
            stage.centerOnScreen(); // Optional

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean IsEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(emailField.getText()).matches();
    }
}

