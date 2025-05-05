package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.regex.Pattern;

public class UserAccountsController {
    @FXML private Button backToDashboardButton; // Sends user back to dashboard.



    @FXML private TextField firstNameField; // Field - User's First Name.
    @FXML private TextField lastNameField; // Field - User's Last Name.
    @FXML private TextField emailField; // Field - User's email.

    private void UpdateUserInfo() {

    }








    private boolean IsEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(emailField.getText()).matches();
    }
}
