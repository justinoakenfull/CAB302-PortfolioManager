package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.IAuthService;
import com.javarepowizards.portfoliomanager.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UserAccountsController implements Initializable {
    @FXML private TextField fNameField;
    @FXML private TextField lNameField;
    @FXML private TextField usernameField; // Field - User's Last Name.
    @FXML private TextField emailField; // Field - User's email.

    @FXML private PasswordField oldPasswordField;

    @FXML private PasswordField newPasswordField;

    @FXML private PasswordField confirmPasswordField;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IAuthService authService;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = AppContext.getService(IUserDAO.class);
        authService = AppContext.getService(IAuthService.class);
    }

    @FXML
    private void updateUserInfo() {
        try {
            String fName = fNameField.getText();
            String lName = lNameField.getText();
            String username = usernameField.getText();
            String email = emailField.getText();
            System.out.println(username);
            if (username.length() > 1) {
                userDAO.updateUsername(Session.getCurrentUser().getUserId(), username);
            }
            if (email.length() > 1 && IsEmail(email)) {
                userDAO.updateEmail(Session.getCurrentUser().getUserId(), email);
            } else if (email.length() > 1 && !IsEmail(email)) {
                throw new Exception("Incorrect email format.");
            }
            if (fName.length() > 1 || lName.length() > 1) {
                userDAO.updateFullName(Session.getCurrentUser().getUserId(), fName, lName);
            }
        } catch (Exception e) {
            showAlertError(e.getMessage());
        }
    }

    @FXML
    private void updateUserPassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        try {
            Optional<User> userOpt = userDAO.getUserById(Session.getCurrentUser().getUserId());
            if (userOpt.isEmpty() || !authService.verifyPassword(oldPassword, userOpt.get().getPasswordHash())) {
                throw new SQLException("Incorrect Password!");
            }
            if (!Objects.equals(newPassword, confirmPassword)) {
                throw new SQLException("New password does not match.");
            }
            if (newPassword.length() < 8) {
                throw new SQLException("Password must be at least 8 characters.");
            }
            userDAO.updatePassword(Session.getCurrentUser().getUserId(), authService.hashPassword(confirmPassword));
        } catch (SQLException e) {
            showAlertError(e.getMessage());
        }
    }

    private void showAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean IsEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}
