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

/**
 * Main entry point for the Portfolio Manager JavaFX application.
 * Initializes core services and displays the login screen.
 */
public class MainApplication extends Application {

    /**
     * Called when the JavaFX application is launched.
     * Initializes application services and loads the login view.
     *
     * @param stage the primary stage for this application
     * @throws IOException           if loading the FXML fails
     * @throws URISyntaxException    if a resource URI is malformed
     * @throws CsvValidationException if CSV parsing fails
     * @throws SQLException          if database initialization fails
     */
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

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}