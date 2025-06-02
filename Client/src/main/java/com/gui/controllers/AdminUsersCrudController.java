package com.gui.controllers;

import com.gui.services.RoleService;
import com.gui.services.UserService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.gui.enums.StagePath;
import com.server.models.Role;
import com.server.models.User;
import com.server.models.Person;
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

public class AdminUsersCrudController implements Initializable {
    @FXML
    private Button openListButton;
    @FXML
    private AnchorPane hiddenBox;
    @FXML
    private Button moviesButton;
    @FXML
    private Button usersButton;
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> patronomicColumn; // Исправлено с patronomicColumn
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField; // Добавлено для ввода пароля
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField patronomicField;
    @FXML
    private ComboBox<Role> roleComboBox;
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

    private UserService userService;
    private RoleService roleService;
    private User selectedUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();
        roleService = new RoleService();
        hiddenBox.setVisible(false);

        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Настройка колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.getPerson() != null ? user.getPerson().getFirstName() : "");
        });
        lastNameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.getPerson() != null ? user.getPerson().getLastName() : "");
        });
        patronomicColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.getPerson() != null && user.getPerson().getPatronomic() != null ? user.getPerson().getPatronomic() : "");
        });
        roleColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.getRole() != null ? user.getRole().getName() : "");
        });

        // Настройка ComboBox для отображения только названия роли
        roleComboBox.setCellFactory(param -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        roleComboBox.setButtonCell(new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // Загрузка ролей и пользователей
        loadRoles();
        loadUsersTable();

        // Настройка слушателя выбора в таблице
        setupTableSelectionObserver();
    }

    private void loadRoles() {
        Response response = roleService.getAll();
        if (response.isSuccess()) {
            List<Role> roles = new Deserializer().extractListData(response.getData(), Role.class);
            roleComboBox.setItems(FXCollections.observableArrayList(roles));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить роли: " + response.getMessage());
        }
    }

    private void loadUsersTable() {
        usersTable.getItems().clear();
        Response response = userService.getAll();
        if (response.isSuccess()) {
            List<User> users = new Deserializer().extractListData(response.getData(), User.class);
            usersTable.setItems(FXCollections.observableArrayList(users));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить пользователей: " + response.getMessage());
        }
    }

    private void setupTableSelectionObserver() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedUser = newValue;
            if (newValue != null) {
                usernameField.setText(newValue.getUsername());
                firstNameField.setText(newValue.getPerson() != null ? newValue.getPerson().getFirstName() : "");
                lastNameField.setText(newValue.getPerson() != null ? newValue.getPerson().getLastName() : "");
                patronomicField.setText((newValue.getPerson() != null && newValue.getPerson().getPatronomic() != null) ? newValue.getPerson().getPatronomic() : "");
                passwordField.clear(); // Не показываем пароль при выборе
                roleComboBox.setValue(newValue.getRole());
                errorLabel.setText("");
            } else {
                clearFields();
            }
        });
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        patronomicField.clear();
        roleComboBox.setValue(null);
        errorLabel.setText("");
        selectedUser = null;
    }

    @FXML
    void onAddButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronomicField.getText().trim();
        Role selectedRole = roleComboBox.getValue();

        // Валидация на стороне клиента
        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || selectedRole == null) {
            errorLabel.setText("Имя пользователя, пароль, имя, фамилия и роль обязательны!");
            return;
        }

        // Создание нового пользователя
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Используем введенный пароль
        newUser.setRole(selectedRole);
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPatronomic(patronymic.isEmpty() ? null : patronymic);
        newUser.setPerson(person);

        // Вызов сервиса для добавления
        Response response = userService.addUser(newUser.getUsername(), newUser.getPassword(),
                newUser.getPerson().getFirstName(),newUser.getPerson().getLastName(),
                newUser.getPerson().getPatronomic(),newUser.getRole());
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Пользователь успешно добавлен");
            loadUsersTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onEditButton(ActionEvent event) {
        if (selectedUser == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите пользователя!");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronomicField.getText().trim();
        Role selectedRole = roleComboBox.getValue();

        // Валидация на стороне клиента
        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || selectedRole == null) {
            errorLabel.setText("Имя пользователя, имя, фамилия и роль обязательны!");
            return;
        }

        // Обновление данных пользователя
        selectedUser.setUsername(username);
        if (!password.isEmpty()) {
            selectedUser.setPassword(password); // Обновляем пароль, если введен
        }
        selectedUser.setRole(selectedRole);
        Person person = selectedUser.getPerson();
        if (person == null) {
            person = new Person();
            selectedUser.setPerson(person);
        }
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPatronomic(patronymic.isEmpty() ? null : patronymic);

        // Вызов сервиса для обновления
        Response response = userService.updateUser(selectedUser);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Данные пользователя успешно обновлены");
            loadUsersTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onDeleteButton(ActionEvent event) {
        if (selectedUser == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите пользователя!");
            return;
        }

        if (AlertUtil.confirmation("Удаление пользователя", "Вы уверены, что хотите удалить пользователя?") != ButtonType.OK) {
            return;
        }

        // Вызов сервиса для удаления
        Response response = userService.delUser(selectedUser.getUsername());
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Пользователь успешно удален");
            loadUsersTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onRefreshButton(ActionEvent event) {
        onEditButton(event); // Перенаправляем на редактирование
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
}