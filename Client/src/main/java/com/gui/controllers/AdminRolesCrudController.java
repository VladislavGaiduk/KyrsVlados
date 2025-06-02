package com.gui.controllers;

import com.gui.services.RoleService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.gui.enums.StagePath;
import com.server.models.Role;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminRolesCrudController implements Initializable {
    @FXML
    private Button openListButton;
    @FXML
    private AnchorPane hiddenBox;
    @FXML
    private Button moviesButton;
    @FXML
    private Button usersButton;
    @FXML
    private Button genresButton;
    @FXML
    private TableView<Role> rolesTable;
    @FXML
    private TableColumn<Role, Integer> idColumn;
    @FXML
    private TableColumn<Role, String> nameColumn;
    @FXML
    private TextField nameField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private RoleService roleService;
    private Role selectedRole;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleService = new RoleService();
        hiddenBox.setVisible(false);

        // Настройка колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        // Загрузка ролей
        loadRolesTable();

        // Настройка слушателя выбора в таблице
        setupTableSelectionObserver();
    }


    private void loadRolesTable() {
        rolesTable.getItems().clear();
        Response response = roleService.getAll();
        if (response.isSuccess()) {
            List<Role> roles = new Deserializer().extractListData(response.getData(), Role.class);
            rolesTable.setItems(FXCollections.observableArrayList(roles));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить роли: " + response.getMessage());
        }
    }

    private void setupTableSelectionObserver() {
        rolesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedRole = newValue;
            if (newValue != null) {
                nameField.setText(newValue.getName());
                errorLabel.setText("");
            } else {
                clearFields();
            }
        });
    }

    private void clearFields() {
        nameField.clear();
        errorLabel.setText("");
        selectedRole = null;
    }

    @FXML
    void onOpenListButton(ActionEvent event) {
        hiddenBox.setVisible(!hiddenBox.isVisible());
        openListButton.setText(hiddenBox.isVisible() ? "Закрыть список" : "Открыть список");
    }

    @FXML
    void onMoviesButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_MOVIES_MANAGMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления фильмами: " + e.getMessage());
        }
    }

    @FXML
    void onUsersButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) usersButton.getScene().getWindow(), StagePath.ADMIN_USERS_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления пользователями: " + e.getMessage());
        }
    }

    @FXML
    void onGenresButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) genresButton.getScene().getWindow(), StagePath.ADMIN_GENRES_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления жанрами: " + e.getMessage());
        }
    }

    @FXML
    void onRolesButton(ActionEvent event) {
        // Already on roles page
    }

    @FXML
    void onAddButton(ActionEvent event) {
        String name = nameField.getText().trim();

        // Валидация
        if (name.isEmpty()) {
            errorLabel.setText("Название роли обязательно!");
            return;
        }

        // Проверка длины названия роли
        if (name.length() > 50) {
            errorLabel.setText("Название роли не должно превышать 50 символов!");
            return;
        }

        // Создание новой роли
        Role newRole = new Role();
        newRole.setName(name);

        // Вызов сервиса для добавления
        Response response = roleService.addRole(newRole);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Роль успешно добавлена");
            loadRolesTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onDeleteButton(ActionEvent event) {
        if (selectedRole == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите роль!");
            return;
        }

        if (AlertUtil.confirmation("Удаление роли", "Вы уверены, что хотите удалить роль?" + 
                "\nЭто действие нельзя отменить.") != ButtonType.OK) {
            return;
        }

        Response response = roleService.deleteRole(selectedRole);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Роль успешно удалена");
            loadRolesTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onRefreshButton(ActionEvent event) {
        if (selectedRole == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите роль!");
            return;
        }

        String name = nameField.getText().trim();

        // Валидация
        if (name.isEmpty()) {
            errorLabel.setText("Название роли обязательно!");
            return;
        }


        // Обновление данных роли
        selectedRole.setName(name);


        // Вызов сервиса для обновления
        Response response = roleService.updateRole(selectedRole);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Данные роли успешно обновлены");
            loadRolesTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }


    @FXML
    void onHallsButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_HALLS_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления залами: " + e.getMessage());
        }
    }

    @FXML
    void onSessionsButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_SESSIONS_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления сеансами: " + e.getMessage());
        }
    }

    @FXML
    void onTicketsButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_TICKETS_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления билетами: " + e.getMessage());
        }
    }
    @FXML
    void onExitButton(ActionEvent event) {
        Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.MAIN_MENU);
    }
}
