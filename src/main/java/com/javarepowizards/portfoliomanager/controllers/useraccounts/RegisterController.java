package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.IAuthService;
import com.javarepowizards.portfoliomanager.services.RegistrationService;
import com.javarepowizards.portfoliomanager.services.ValidationException;
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

import static java.util.logging.Level.INFO;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

@Component
public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField balanceField;

    @Autowired
    private IAuthService authService;


    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private IUserDAO userDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = AppContext.getService(IUserDAO.class);
        authService = AppContext.getService(IAuthService.class);
        registrationService = new RegistrationService(authService, userDAO);
    }

    /**
     * Handles the registration process when the user clicks the "Register" button.
     * Validates input fields, hashes the password, and creates a new user account.
     */

    @FXML
    private void handleRegister() {

        String username        = usernameField.getText().trim();
        String email           = emailField   .getText().trim();
        String rawPassword     = passwordField.getText();
        String rawConfirm      = confirmPasswordField.getText();
        String rawBalance      = balanceField .getText().trim();

        try {
            registrationService.register(
                    username,
                    email,
                    rawPassword,
                    rawConfirm,
                    rawBalance
            );

            showAlert(Alert.AlertType.INFORMATION,
                    "Success",
                    "Account created successfully!");
            clearForm();
            switchToLogin();

        } catch (ValidationException ve) {
            showAlert(Alert.AlertType.WARNING,
                    "Validation Error",
                    ve.getMessage());
        } catch (SQLException se) {
            showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    se.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "An unexpected error occurred: " + ex.getMessage());
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
            showAlert(ERROR, "Navigation Error", "Could not load login page");
        }
    }

    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        balanceField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
