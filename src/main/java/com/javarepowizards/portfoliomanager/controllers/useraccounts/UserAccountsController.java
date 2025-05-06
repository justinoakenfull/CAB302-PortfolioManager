package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.IAuthService;
import com.javarepowizards.portfoliomanager.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    @FXML private TextField nameField; // TODO: Add handling
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
        String username = usernameField.getText();
        String email = emailField.getText();
        try {
            if (username != null) {
                userDAO.updateUsername(Session.getCurrentUser().getUserId(), username);
            }
            if (email != null && IsEmail(email)) {

                userDAO.updateEmail(Session.getCurrentUser().getUserId(), email);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateUserPassword() throws SQLException {
        String username  = Session.getCurrentUser().getUsername();
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            Optional<User> userOpt = userDAO.getUserByUsername(username);
            if (userOpt.isEmpty() || !authService.verifyPassword(oldPassword, userOpt.get().getPasswordHash())) {
                System.out.println("Incorrect Password!");
            }
            if (!Objects.equals(newPassword, confirmPassword)) {
                System.out.println("New password does not match.");
            }
            userDAO.updatePassword(Session.getCurrentUser().getUserId(), authService.hashPassword(confirmPassword));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private boolean IsEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(emailField.getText()).matches();
    }



}
