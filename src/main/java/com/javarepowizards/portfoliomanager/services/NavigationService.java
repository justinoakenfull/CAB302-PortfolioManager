package com.javarepowizards.portfoliomanager.services;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.function.Consumer;


public class NavigationService {
    private final StackPane contentArea;
    private static final String basePath = "src/main/resources/com/javarepowizards/portfoliomanager/views/";


    public NavigationService(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public <T> void loadView(String relativeFxmlPath, Consumer<T> controllerInit) {
       String fullPath = basePath + relativeFxmlPath;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fullPath));
            Parent view = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            controllerInit.accept(controller);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fullPath, e);
        }
    }

    public static <T> void loadScene(Node sourceNode,
                                     String relativeFxmlPath,
                                     Consumer<T> controllerInit,
                                     String windowTitle,
                                     double width,
                                     double height)
    {
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
