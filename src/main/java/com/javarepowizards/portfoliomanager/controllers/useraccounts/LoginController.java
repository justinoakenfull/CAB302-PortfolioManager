package com.javarepowizards.portfoliomanager.controllers.useraccounts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class LoginController {

    @FXML
    private javafx.scene.control.TextField emailField;

    @FXML
    private javafx.scene.control.PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {

        // skips actual login process for now, just goes to next screen.
        Parent root = FXMLLoader.load(getClass().getResource("/views/main-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
}
}
