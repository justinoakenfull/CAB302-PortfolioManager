package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.services.Auth.RegistrationService;
import com.javarepowizards.portfoliomanager.services.utility.ValidationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static javafx.scene.control.Alert.AlertType.ERROR;


/**
 * Controller for the registration view.
 * Handles user input validation, creation of new user accounts,
 * and navigation back to the login screen.
 * Retrieves required services from the application context.
 */
public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField balanceField;

    private RegistrationService registrationService;

    /**
     * Initializes the controller after its FXML fields have been injected.
     * Obtains the registration service from the application context.
     *
     * @param url            the location used to resolve relative paths, or null if unknown
     * @param resourceBundle the resources used to localize the root object, or null if none
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        registrationService = AppContext.getService(RegistrationService.class);
    }

    /**
     * Handles the register button action.
     * Validates all inputs, attempts to create a new user with the specified
     * starting balance, and shows success or error alerts.
     * On successful registration, clears the form and switches to the login view.
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


    /**
     * Switches the current scene to the login view.
     * Loads the login FXML, replaces the root of the current scene,
     * and updates the stage title and position.
     */
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

    /**
     * Clears all input fields on the registration form.
     */
    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        balanceField.clear();
    }

    /**
     * Displays an alert dialog with the specified type, title, and message.
     *
     * @param alertType the type of alert to display
     * @param title the title of the alert dialog
     * @param message the content message of the alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
