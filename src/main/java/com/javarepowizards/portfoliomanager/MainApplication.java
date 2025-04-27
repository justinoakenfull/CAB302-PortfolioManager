package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.*;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.infrastructure.InMemoryStockRepository;
import com.javarepowizards.portfoliomanager.models.*;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException, CsvValidationException, SQLException {
        // Initialize core services first
        initializeDatabaseServices();
        initializeStockRepository();
        initializeWatchlist();

        // Load the login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/login.fxml"));
        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();

        /*
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        stage.show(); */
    }

    private void initializeDatabaseServices() throws SQLException {
        IDatabaseConnection dbConnection = new DatabaseConnection();
        AppContext.registerService(IDatabaseConnection.class, dbConnection);

        // Initialize UserDAO since other services might depend on it
        UserDAO userDAO = new UserDAO(dbConnection);
        AppContext.registerService(UserDAO.class, userDAO);
    }

    private void initializeStockRepository() throws URISyntaxException, CsvValidationException, IOException {
        URL csvUrl = getClass().getResource("/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");
        if (csvUrl == null) {
            throw new IllegalStateException("CSV file not found");
        }
        Path csvPath = Paths.get(csvUrl.toURI());
        StockRepository repo = new InMemoryStockRepository(csvPath);
        AppContext.initStockRepository(repo);
    }

    private void initializeWatchlist() throws SQLException {
        IDatabaseConnection dbConnection = AppContext.getService(IDatabaseConnection.class);
        IWatchlistDAO watchlistDAO = new WatchlistDAO(dbConnection); // Concrete class, but variable type is interface
        StockRepository repo = AppContext.getService(StockRepository.class);

        Watchlist watchlist = new Watchlist(repo, watchlistDAO, 1); // Default user ID
        watchlist.refresh();

        AppContext.registerService(IWatchlistDAO.class, watchlistDAO); // Register interface
        AppContext.registerService(Watchlist.class, watchlist);
    }

    public static void main(String[] args) {
        launch();
    }
}