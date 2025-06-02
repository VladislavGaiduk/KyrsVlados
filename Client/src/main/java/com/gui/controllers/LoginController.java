package com.gui.controllers;

import com.gui.enums.StagePath;
import com.gui.services.UserService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.server.models.User;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.serializer.Deserializer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    private UserService userService;

    @FXML
    void onBackButton()
    {
        Loader.loadScene((Stage) loginButton.getScene().getWindow(), StagePath.MAIN_MENU);
    }
    @FXML
    void onLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Response response = userService.login(username, password);

        if (response.isSuccess()) {
            User currentUser = new Deserializer().extractData(response.getData(), User.class);
            ServerClient.setCurrentUser(currentUser);

            // Проверка роли пользователя
            String roleName = currentUser.getRole().getName().toLowerCase();
            StagePath targetStage;
            if ("admin".equals(roleName)) {
                targetStage = StagePath.ADMIN_MOVIES_MANAGMENT;
            } else if ("user".equals(roleName)) {
                 targetStage = StagePath.ADMIN_MOVIES_MANAGMENT;
            } else {
                AlertUtil.error("Login Error", "Unknown role: " + roleName);
                return;
            }

            Loader.loadScene((Stage) loginButton.getScene().getWindow(), targetStage);
        } else {
            AlertUtil.error("Login Error", response.getMessage());
        }

//            Loader.loadScene((Stage) LoginButton.getScene().getWindow(), StagePath.ADMIN_MOVIES_MANAGMENT);

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.userService = new UserService();
    }
}