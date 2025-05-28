package com.javarepowizards.portfoliomanager;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException, CsvValidationException, SQLException {

        AppContext.initAll();

        // Load the login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/login.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Login");
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}