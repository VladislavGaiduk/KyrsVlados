package com;

import com.gui.enums.StagePath;
import com.gui.utils.Loader;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.server.network.ServerClient;



import java.io.IOException;
import java.util.Objects;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("PixelHall");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));

        try {
            Loader.loadSceneWithThrowException(primaryStage, StagePath.MAIN_MENU);
        } catch (IOException e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }

        primaryStage.show();
    }

    @Override
    public void stop() {
        ServerClient client = ServerClient.getInstance();
        if (client != null) {
            client.disconnect();
        }


    }

    public static void main(String[] args) {
        launch(args);
    }
}