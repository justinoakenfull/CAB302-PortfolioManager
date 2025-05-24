package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.*;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.infrastructure.InMemoryStockRepository;
import com.javarepowizards.portfoliomanager.services.*;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException, CsvValidationException, SQLException {
        // Initialize core services first
        initializeDatabaseServices();
        initializeStockRepository();
        initializeWatchlist();
        initializeAuthService();

        // Load the login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/login.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Login");
        stage.show();
    }

    private void initializeDatabaseServices() throws SQLException {
        // 1) DB connection, user DAO
        IDatabaseConnection dbConnection = new DatabaseConnection();
        AppContext.registerService(IDatabaseConnection.class, dbConnection);

        // Initialize UserDAO since other services might depend on it
        IUserDAO userDAO = new UserDAO(dbConnection);
        AppContext.registerService(IUserDAO.class, userDAO);


        // register the PortfolioDAO under its interface
        PortfolioDAO portfolioDAO = new PortfolioDAO(dbConnection);
        AppContext.registerService(IPortfolioDAO.class, portfolioDAO);
        System.out.println("DB connection: " + dbConnection.getConnection());
    }

    private void initializeAuthService() {
        PasswordEncoder pwEncoder = new BCryptPasswordEncoder();
        IAuthService authService = new AuthService(pwEncoder);
        AppContext.registerService(IAuthService.class, authService);
    }

    private void initializeStockRepository() throws URISyntaxException, CsvValidationException, IOException {
        URL priceCsvUrl = getClass().getResource("/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");
        URL descUrl   = getClass().getResource("/com/javarepowizards/portfoliomanager/data/descriptions.csv");
        if (priceCsvUrl == null || descUrl == null) {
            throw new IllegalStateException("CSV file(s) not found");
        }
        Path csvPath = Paths.get(priceCsvUrl.toURI());
        Path csvDescPath = Paths.get(descUrl.toURI());
        StockRepository repo = new InMemoryStockRepository(csvPath, csvDescPath);
        AppContext.initStockRepository(repo);

        StockDAO stockDAO = StockDAO.getInstance();
        AppContext.registerService(StockDAO.class, stockDAO);
    }

    private void initializeWatchlist() throws SQLException {
        //construct & register the DAO
        IWatchlistDAO watchlistDAO = new WatchlistDAO(AppContext.getService(IDatabaseConnection.class));
        AppContext.registerService(IWatchlistDAO.class, watchlistDAO);

        //construct & register the application service
        WatchlistService watchlistService =
                new WatchlistService(
                        AppContext.getService(StockRepository.class),
                        watchlistDAO,
                        AppContext.getService(IUserDAO.class),
                        AppContext.getService(IPortfolioDAO.class));
        AppContext.registerService(IWatchlistService.class, watchlistService);
    }






    public static void main(String[] args) {
        launch();
    }
}