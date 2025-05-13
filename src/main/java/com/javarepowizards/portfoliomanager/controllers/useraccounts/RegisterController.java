package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.IAuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

@Component
public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @Autowired
    private IAuthService authService;

    @Autowired
    private IUserDAO userDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = AppContext.getService(IUserDAO.class);
        authService = AppContext.getService(IAuthService.class);
    }

    @FXML
    private void handleRegister() {
        if (!validateInputs()) return;

        try {
            User user = new User(
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    authService.hashPassword(passwordField.getText())
            );

            if (userDAO.createUser(user)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
                clearForm();
                switchToLogin();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not create user: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/login.fxml"));
            Parent newRoot = loader.load();

            Scene currentScene = usernameField.getScene();
            currentScene.setRoot(newRoot);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load login page");
        }
    }

    private boolean validateInputs() {
        if (usernameField.getText().isBlank() || emailField.getText().isBlank()
                || passwordField.getText().isBlank() || confirmPasswordField.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields are required");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords do not match");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password must be at least 8 characters");
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(emailField.getText()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid email format");
            return false;
        }

        return true;
    }

    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
