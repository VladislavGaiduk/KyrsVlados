package com.gui.controllers;

import com.gui.enums.StagePath;
import com.gui.utils.Loader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class StartMenuController {



    @FXML
    private Button loginButton;

    @FXML
    private Button Exit;

    @FXML
    public void onRegistrationButton(javafx.event.ActionEvent event) {
        Loader.loadScene((Stage) loginButton.getScene().getWindow(), StagePath.REGISTRATION);
    }

    @FXML
    public void onLoginButton(javafx.event.ActionEvent event) {
        Loader.loadScene((Stage) loginButton.getScene().getWindow(), StagePath.LOGIN);
    }

    @FXML
    public void onExit() {
        Stage stage = (Stage) Exit.getScene().getWindow();
        stage.close();
    }
}
