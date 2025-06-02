package com.gui.controllers;

import com.gui.enums.StagePath;
import com.gui.services.UserService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.server.network.Response;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    private UserService userService;

    @FXML
    private  TextField usernameField;
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registrationButton;



    @FXML
    void onRegistrationButton(javafx.event.ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String emails = emailField.getText();

        if (!password.equals(confirmPassword)) {
            AlertUtil.error("Registration Error", "Passwords do not match.");
            return;
        }

        Response response = userService.register(username, password, firstName, lastName,emails,null);

        if (response.isSuccess()) {
            AlertUtil.info("Registration Successful", "You have registered successfully.");

            Loader.loadScene((Stage) registrationButton.getScene().getWindow(), StagePath.LOGIN);
        } else {
            AlertUtil.error("Registration Error", response.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.userService = new UserService();
    }

    @FXML
    public void onBackButton(javafx.event.ActionEvent event) {
        Loader.loadScene((Stage) registrationButton.getScene().getWindow(), StagePath.MAIN_MENU);
    }

}

