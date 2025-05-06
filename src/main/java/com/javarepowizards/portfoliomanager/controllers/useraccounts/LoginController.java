package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.MainController;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.IAuthService;
import com.javarepowizards.portfoliomanager.services.Session;
import com.javarepowizards.portfoliomanager.services.NavigationService;


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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

@Component
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @Autowired
    private IAuthService authService;

    @Autowired
    private IUserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = AppContext.getUserDAO();
        authService = AppContext.getService(IAuthService.class);
    }



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



            if (userOpt.isEmpty() ||
                    !authService.verifyPassword(password, userOpt.get().getPasswordHash())) {
                showAlert("Login Failed", "Invalid username/email or password.");
                return;
            }

            Session.setCurrentUser(userOpt.get());




            // —— swap in the “shell” with nav bar ——
            NavigationService.loadScene(
                    /* source node */   loginButton,
                    /* fxml path */     "hello-view.fxml",
                    /* controller init */ ctrl -> {
                        // no extra setup: MainController.initialize()
                        // will automatically fire and load the dashboard
                    },
                    /* title */         "Dashboard",
                    /* width */         1200,
                    /* height */        800
            );

        } catch (SQLException e) {
            showAlert("Database Error", "Could not verify credentials: " + e.getMessage());

        }
    }

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

    @FXML
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/hello-view.fxml"));
            Parent newRoot = loader.load();

            Scene currentScene = loginButton.getScene();
            currentScene.setRoot(newRoot);

            MainController mainController = loader.getController();
            mainController.showDashboard();

            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Dashboard");
            stage.setWidth(1920);
            stage.setHeight(1080);
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Error: ", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isEmail(String input) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(input).matches();
    }
}
