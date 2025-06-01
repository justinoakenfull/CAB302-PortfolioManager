package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.user.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.Auth.IAuthService;
import com.javarepowizards.portfoliomanager.services.session.Session;
import com.javarepowizards.portfoliomanager.services.session.NavigationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controller for the login view.
 * Handles user authentication, transitions to registration,
 * and provides a shortcut for a dummy login to a test account.
 * Retrieves required services from the application context.
 */

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private IAuthService authService;
    private IUserDAO userDAO;

    /**
     * Initializes the controller after FXML components are loaded.
     * Obtains UserDAO and AuthService instances from the application context.
     *
     * @param location  the location used to resolve relative paths, or null if unknown
     * @param resources the resources used to localize the root object, or null if none
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = AppContext.getUserDAO();
        authService = AppContext.getService(IAuthService.class);
    }

    /**
     * Handles the login button action.
     * Validates input, retrieves the user by email or username,
     * verifies the password, and navigates to the dashboard on success.
     * Shows an error alert on failure or database issues.
     */
    @FXML
    private void handleLogin() {

        String email = emailField.getText(), password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username/email and password.");
            return;
        }

        try {
            Optional<User> userOpt = isEmail(email)
                    ? userDAO.getUserByEmail(email)
                    : userDAO.getUserByUsername(email);

            if (userOpt.isPresent() && authService.verifyPassword(password, userOpt.get().getPasswordHash())) {
                Session.setCurrentUser(userOpt.get());

                // —— swap in the “shell” with nav bar ——
                NavigationService.loadScene(
                        /* source node */   loginButton,
                        /* fxml path */     "navigation-bar.fxml",
                        /* controller init */ ctrl -> {
                            // no extra setup: MainController.initialize()
                            // will automatically fire and load the dashboard
                        },
                        /* title */         "Dashboard",
                        /* width */         1200,
                        /* height */        800
                );
            } else {
                showAlert("Login Failed", "Invalid username/email or password.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not verify credentials: " + e.getMessage());
        }
    }

    /**
     * Switches the current scene to the registration view.
     * Loads the registration FXML, replaces the root of the current scene,
     * and updates the stage title.
     */
    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/registration.fxml"));
            Parent newRoot = loader.load();

            Scene currentScene = emailField.getScene();
            currentScene.setRoot(newRoot);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Register");
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Error: ", e.getMessage());
        }
    }

    /**
     * Displays an error alert with the given title and message.
     *
     * @param title   the title of the alert dialog
     * @param message the content message of the alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Determines if the given input string matches an email pattern.
     *
     * @param input the string to test
     * @return true if the input is a valid email format, false otherwise
     */
    private boolean isEmail(String input) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(input).matches();
    }
}
