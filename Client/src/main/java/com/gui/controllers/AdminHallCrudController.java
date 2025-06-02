package com.gui.controllers;

import com.gui.services.HallService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.gui.enums.StagePath;
import com.server.models.Hall;
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

public class AdminHallCrudController implements Initializable {
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
    private TableView<Hall> hallsTable;
    @FXML
    private TableColumn<Hall, Integer> idColumn;
    @FXML
    private TableColumn<Hall, String> nameColumn;
    @FXML
    private TableColumn<Hall, Integer> capacityColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextField capacityField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label errorLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private HallService hallService;
    private Hall selectedHall;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hallService = new HallService();
        hiddenBox.setVisible(false);

        // Настройка колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        // Загрузка залов
        loadHallsTable();

        // Настройка слушателя выбора в таблице
        setupTableSelectionObserver();
    }


    private void loadHallsTable() {
        hallsTable.getItems().clear();
        Response response = hallService.getAll();
        if (response.isSuccess()) {
            List<Hall> halls = new Deserializer().extractListData(response.getData(), Hall.class);
            hallsTable.setItems(FXCollections.observableArrayList(halls));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить залы: " + response.getMessage());
        }
    }

    private void setupTableSelectionObserver() {
        hallsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedHall = newValue;
            if (newValue != null) {
                nameField.setText(newValue.getName());
                capacityField.setText(String.valueOf(newValue.getCapacity()));
                descriptionArea.setText(newValue.getDescription() != null ? newValue.getDescription() : "");
                errorLabel.setText("");
            } else {
                clearFields();
            }
        });
    }

    private void clearFields() {
        nameField.clear();
        capacityField.clear();
        descriptionArea.clear();
        errorLabel.setText("");
        selectedHall = null;
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
    void onRolesButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_ROLES_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления жанрами: " + e.getMessage());
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
    void onHallsButton(ActionEvent event) {
        // Уже на странице залов
    }

    @FXML
    void onAddButton(ActionEvent event) {
        String name = nameField.getText().trim();
        String capacityText = capacityField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Валидация
        if (name.isEmpty()) {
            errorLabel.setText("Название зала обязательно!");
            return;
        }

        if (capacityText.isEmpty()) {
            errorLabel.setText("Вместимость зала обязательна!");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Вместимость должна быть положительным числом!");
            return;
        }

        // Создание нового зала
        Hall newHall = new Hall();
        newHall.setName(name);
        newHall.setCapacity(capacity);
        newHall.setDescription(description.isEmpty() ? null : description);

        // Вызов сервиса для добавления
        Response response = hallService.addHall(newHall);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Зал успешно добавлен");
            loadHallsTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onDeleteButton(ActionEvent event) {
        if (selectedHall == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите зал!");
            return;
        }

        if (AlertUtil.confirmation("Удаление зала", "Вы уверены, что хотите удалить зал?" + 
                "\nЭто действие нельзя отменить.") != ButtonType.OK) {
            return;
        }

        Response response = hallService.deleteHall(selectedHall);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Зал успешно удален");
            loadHallsTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onRefreshButton(ActionEvent event) {
        if (selectedHall == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите зал!");
            return;
        }

        String name = nameField.getText().trim();
        String capacityText = capacityField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Валидация
        if (name.isEmpty()) {
            errorLabel.setText("Название зала обязательно!");
            return;
        }

        if (capacityText.isEmpty()) {
            errorLabel.setText("Вместимость зала обязательна!");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Вместимость должна быть положительным числом!");
            return;
        }

        // Обновление данных зала
        selectedHall.setName(name);
        selectedHall.setCapacity(capacity);
        selectedHall.setDescription(description.isEmpty() ? null : description);

        // Вызов сервиса для обновления
        Response response = hallService.updateHall(selectedHall);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Данные зала успешно обновлены");
            loadHallsTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
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
