package com.gui.controllers;

import com.gui.services.GenreService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.gui.enums.StagePath;
import com.server.models.Genre;
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

public class AdminGenresCrudController implements Initializable {
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
    private TableView<Genre> genresTable;
    @FXML
    private TableColumn<Genre, Integer> idColumn;
    @FXML
    private TableColumn<Genre, String> nameColumn;
    @FXML
    private TextField nameField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private GenreService genreService;
    private Genre selectedGenre;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        genreService = new GenreService();
        hiddenBox.setVisible(false);

        genresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Настройка колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        // Загрузка жанров
        loadGenresTable();

        // Настройка слушателя выбора в таблице
        setupTableSelectionObserver();
    }

    private void loadGenresTable() {
        genresTable.getItems().clear();
        Response response = genreService.getAll();
        if (response.isSuccess()) {
            List<Genre> genres = new Deserializer().extractListData(response.getData(), Genre.class);
            genresTable.setItems(FXCollections.observableArrayList(genres));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить жанры: " + response.getMessage());
        }
    }

    private void setupTableSelectionObserver() {
        genresTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedGenre = newValue;
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
        selectedGenre = null;
    }


    @FXML
    void onAddButton(ActionEvent event) {
        String name = nameField.getText().trim();

        // Валидация на стороне клиента
        if (name.isEmpty()) {
            errorLabel.setText("Название жанра обязательно!");
            return;
        }

        // Проверка длины имени жанра (предполагаем, что в базе данных поле name имеет ограничение VARCHAR(50))
        if (name.length() > 50) {
            errorLabel.setText("Название жанра не должно превышать 50 символов!");
            return;
        }

        // Создание нового жанра
        Genre newGenre = new Genre();
        newGenre.setName(name);

        // Вызов сервиса для добавления
        Response response = genreService.addGenre(newGenre);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Жанр успешно добавлен");
            loadGenresTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onEditButton(ActionEvent event) {
        if (selectedGenre == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите жанр!");
            return;
        }

        String name = nameField.getText().trim();

        // Валидация на стороне клиента
        if (name.isEmpty()) {
            errorLabel.setText("Название жанра обязательно!");
            return;
        }

        // Проверка длины имени жанра
        if (name.length() > 50) {
            errorLabel.setText("Название жанра не должно превышать 50 символов!");
            return;
        }

        // Обновление данных жанра
        selectedGenre.setName(name);

        // Вызов сервиса для обновления
        Response response = genreService.updateGenre(selectedGenre);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Данные жанра успешно обновлены");
            loadGenresTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onDeleteButton(ActionEvent event) {
        if (selectedGenre == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите жанр!");
            return;
        }

        if (AlertUtil.confirmation("Удаление жанра", "Вы уверены, что хотите удалить жанр?") != ButtonType.OK) {
            return;
        }

        // Вызов сервиса для удаления
        Response response = genreService.deleteGenre(selectedGenre);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Жанр успешно удален");
            loadGenresTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onRefreshButton(ActionEvent event) {
        onEditButton(event);
    }

    @FXML
    void onOpenListButton(ActionEvent event) {
        hiddenBox.setVisible(!hiddenBox.isVisible());
        openListButton.setText(hiddenBox.isVisible() ? "Закрыть список" : "Открыть список");
    }

    @FXML
    void onUsersButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_USERS_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления пользователями: " + e.getMessage());
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
    void onMoviesButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_MOVIES_MANAGMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления фильмами: " + e.getMessage());
        }
    }
    @FXML
    void onGenresButton(ActionEvent event) {
        try {
            Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_GENRES_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления жанрами: " + e.getMessage());
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