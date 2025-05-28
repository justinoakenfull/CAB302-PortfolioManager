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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


/**
 * Controller for the registration view.
 * Handles user input validation, creation of new user accounts,
 * and navigation back to the login screen.
 * Retrieves required services from the application context.
 */
@Component
public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField balanceField;

    private IAuthService authService;
    private IUserDAO userDAO;

    /**
     * Initializes the controller after FXML components are loaded.
     * Obtains UserDAO and AuthService instances from the application context.
     *
     * @param url the location used to resolve relative paths, or null if unknown
     * @param resourceBundle the resources used to localize the root object, or null if none
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = AppContext.getService(IUserDAO.class);
        authService = AppContext.getService(IAuthService.class);
    }

    /**
     * Handles the register button action.
     * Validates all inputs, attempts to create a new user with the specified
     * starting balance, and shows success or error alerts.
     * On successful registration, clears the form and switches to the login view.
     */
    @FXML
    private void handleRegister() {
        if (!validateInputs()) return;

        double startingBalance = Double.parseDouble(balanceField.getText().trim());

        try {
            User user = new User(
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    authService.hashPassword(passwordField.getText())
            );

            if (userDAO.createUser(user, startingBalance)) {
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load login page");
        }
    }

    /**
     * Validates user input fields.
     * Checks for non-empty fields, matching passwords, minimum password length,
     * valid email format, and a non-negative numeric balance.
     *
     * @return true if all inputs are valid; false otherwise
     */
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

        if (balanceField.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Balance field cannot be empty");
            return false;
        }

        try {
            double balance = Double.parseDouble(balanceField.getText());
            if (balance < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Balance cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Balance must be a valid number");
            return false;
        }

        return true;
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
