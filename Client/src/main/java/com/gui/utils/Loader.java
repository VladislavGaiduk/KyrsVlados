package com.gui.utils;

import com.gui.enums.StagePath;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.io.IOException;
import java.util.Objects;

public class Loader {

    public static void loadSceneWithThrowException(Stage stage, StagePath stagePath) throws IOException {
        String path = "/" + stagePath.getPathToFxml();
        URL resource = Loader.class.getResource(path);

        if (resource == null) {
            System.err.println("Resource not found: " + path); // Логирование ошибки
            throw new IOException("Resource not found: " + path);
        }

        System.out.println("Resource found: " + resource); // Логирование успешной загрузки
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        stage.setResizable(false);
        Scene scene = new Scene(root);

        stage.setScene(scene);
    }

    public static void loadScene(Stage stage, StagePath stagePath) {
        try {
            loadSceneWithThrowException(stage, stagePath);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.error("Navigation Error", "Could not navigate.");
        }
    }
}