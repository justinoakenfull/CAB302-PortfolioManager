package com.javarepowizards.portfoliomanager.services.session;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Service for navigating between views and scenes in the JavaFX application.
 * Supports loading FXML into a designated content area or replacing the entire scene.
 */
public class NavigationService {
    private final StackPane contentArea;
    private static final String basePath = "/com/javarepowizards/portfoliomanager/views/";


    /**
     * Constructor for NavigationService.
     *
     * @param contentArea The StackPane where the views will be loaded.
     */

    public NavigationService(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Loads a new view into the content area.
     *
     * @param relativeFxmlPath The relative path to the FXML file.
     * @param controllerInit A consumer to initialize the controller.
     * @param <T> The type of the controller.
     */
    public <T> void loadView(String relativeFxmlPath, Consumer<T> controllerInit) {
        String fullPath = basePath + relativeFxmlPath;
        URL fxmlUrl = getClass().getResource(fullPath);
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML file not found: " + fullPath);
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            controllerInit.accept(controller);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fullPath, e);
        }

    }

    /**
     * Loads a new scene in the current window.
     *
     * @param sourceNode The node from which to get the current stage.
     * @param relativeFxmlPath The relative path to the FXML file.
     * @param controllerInit A consumer to initialize the controller.
     * @param windowTitle The title of the new window.
     * @param width The width of the new window.
     * @param height The height of the new window.
     */

    public static <T> void loadScene (Node sourceNode, String relativeFxmlPath, Consumer<T> controllerInit, String windowTitle, double width, double height) {
        String fullPath = basePath + relativeFxmlPath;
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationService.class.getResource(fullPath)
            );
            Parent root = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            controllerInit.accept(controller);

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fullPath, e);
        }
    }

}