package com.gui.controllers;

import com.gui.services.SessionService;
import com.gui.services.MovieService;
import com.gui.services.HallService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.gui.enums.StagePath;
import com.server.models.Hall;
import com.server.models.Movie;
import com.server.models.Session;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import javafx.beans.property.SimpleObjectProperty;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSessionsCrudController implements Initializable {
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
    private Button rolesButton;
    @FXML
    private TableView<Session> sessionsTable;
    @FXML
    private TableColumn<Session, Integer> idColumn;
    @FXML
    private TableColumn<Session, String> movieColumn;
    @FXML
    private TableColumn<Session, String> hallColumn;
    @FXML
    private TableColumn<Session, String> startTimeColumn;
    @FXML
    private TableColumn<Session, String> endTimeColumn;
    @FXML
    private TableColumn<Session, Float> priceColumn;
    @FXML
    private ComboBox<Movie> movieComboBox;
    @FXML
    private ComboBox<Hall> hallComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private ComboBox<Integer> startHourCombo;
    @FXML
    private ComboBox<Integer> startMinuteCombo;

    @FXML
    private TextField priceField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private SessionService sessionService;
    private MovieService movieService;
    private HallService hallService;
    private Session selectedSession;

    private void initializeTimeCombos() {
        // Initialize hours (0-23)
        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        
        // Initialize minutes (0, 15, 30, 45)
        List<Integer> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i += 15) {
            minutes.add(i);
        }
        
        // Configure start time combos
        startHourCombo.getItems().setAll(hours);
        startMinuteCombo.getItems().setAll(minutes);
        
        // Set cell factories to display values properly
        startHourCombo.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%02d", item));
            }
        });
        
        startMinuteCombo.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%02d", item));
            }
        });
        
        // Set button cells to display selected value
        startHourCombo.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Час" : String.format("%02d", item));
            }
        });
        
        startMinuteCombo.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Мин" : String.format("%02d", item));
            }
        });
        
        // Set default time to current time rounded to nearest 15 minutes
        LocalTime now = LocalTime.now();
        int currentMinute = now.getMinute();
        int roundedMinute = (currentMinute / 15) * 15;
        
        startHourCombo.getSelectionModel().select(Integer.valueOf(now.getHour()));
        startMinuteCombo.getSelectionModel().select(Integer.valueOf(roundedMinute));
        
        // Set default date to today
        startDatePicker.setValue(LocalDate.now());
        
        // Add listener to update end time when movie or start time changes
        movieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateEndTime());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateEndTime());
        startHourCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateEndTime());
        startMinuteCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateEndTime());
    }
    
    private LocalDateTime calculateEndTime() {
        LocalDateTime startDateTime = getStartDateTime();
        if (startDateTime != null && movieComboBox.getValue() != null) {
            // Calculate end time based on movie duration
            int durationMinutes = movieComboBox.getValue().getDurationMinutes();
            return startDateTime.plusMinutes(durationMinutes);
        }
        return null;
    }
    
    private void updateEndTime() {
        // This method is called when start time or movie changes
        // The end time will be calculated when needed in getEndDateTime()
    }
    
    private LocalDateTime getStartDateTime() {
        if (startDatePicker.getValue() == null || 
            startHourCombo.getValue() == null || 
            startMinuteCombo.getValue() == null) {
            return null;
        }
        return LocalDateTime.of(
            startDatePicker.getValue(),
            LocalTime.of(startHourCombo.getValue(), startMinuteCombo.getValue())
        );
    }
    
    private LocalDateTime getEndDateTime() {
        return calculateEndTime();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing AdminSessionsCrudController...");

        sessionService = new SessionService();
        movieService = new MovieService();
        hallService = new HallService();
        
        // Initialize time selection
        initializeTimeCombos();

        System.out.println("Loading movies and halls...");
        loadMovies();
        loadHalls();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        movieColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue().getMovie();
            return new SimpleStringProperty(movie != null ? movie.getTitle() : "");
        });
        hallColumn.setCellValueFactory(cellData -> {
            Hall hall = cellData.getValue().getHall();
            return new SimpleStringProperty(hall != null ? hall.getName() : "");
        });
        startTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
        );
        endTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
        );
        priceColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPrice())
        );
        
        // Set cell factory for price column to format as currency
        priceColumn.setCellFactory(column -> new TableCell<Session, Float>() {
            @Override
            protected void updateItem(Float price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₽", price));
                }
            }
        });

        loadSessionsTable();
        setupTableSelectionObserver();
    }

    private void loadSessionsTable() {
        sessionsTable.getItems().clear();
        Response response = sessionService.getAllSessions();
        if (response.isSuccess()) {
            List<Session> sessions = new Deserializer().extractListData(response.getData(), Session.class);
            sessionsTable.setItems(FXCollections.observableArrayList(sessions));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить сеансы: " + response.getMessage());
        }
    }

    private void setupTableSelectionObserver() {
        sessionsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedSession = newValue;
            if (newValue != null) {
                movieComboBox.setValue(newValue.getMovie());
                hallComboBox.setValue(newValue.getHall());
                
                // Set start time
                startDatePicker.setValue(newValue.getStartTime().toLocalDate());
                startHourCombo.getSelectionModel().select(Integer.valueOf(newValue.getStartTime().getHour()));
                startMinuteCombo.getSelectionModel().select(Integer.valueOf((newValue.getStartTime().getMinute() / 15) * 15));
                
                // End time will be calculated automatically
                
                priceField.setText(String.format("%.2f", newValue.getPrice()));
                errorLabel.setText("");
            } else {
                clearFields();
            }
        });
    }

    private void loadMovies() {
        try {
            Response response = movieService.getAll();
            if (response != null && response.isSuccess()) {
                List<Movie> movies = new Deserializer().extractListData(response.getData(), Movie.class);
                if (movies != null && !movies.isEmpty()) {
                    // Простое добавление элементов
                    movieComboBox.getItems().clear();
                    movieComboBox.getItems().addAll(movies);
                    movieComboBox.setCellFactory(lv -> new ListCell<Movie>() {
                        @Override
                        protected void updateItem(Movie movie, boolean empty) {
                            super.updateItem(movie, empty);
                            setText(movie == null ? "" : movie.getTitle());
                        }
                    });
                    movieComboBox.setButtonCell(new ListCell<Movie>() {
                        @Override
                        protected void updateItem(Movie movie, boolean empty) {
                            super.updateItem(movie, empty);
                            setText(empty || movie == null ? "Выберите фильм" : movie.getTitle());
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHalls() {
        try {
            Response response = hallService.getAll();
            if (response != null && response.isSuccess()) {
                List<Hall> halls = new Deserializer().extractListData(response.getData(), Hall.class);
                if (halls != null && !halls.isEmpty()) {
                    // Clear existing items and add all halls
                    hallComboBox.getItems().clear();
                    hallComboBox.getItems().addAll(halls);
                    
                    // Set cell factory to display hall name in dropdown menu
                    hallComboBox.setCellFactory(lv -> new ListCell<Hall>() {
                        @Override
                        protected void updateItem(Hall hall, boolean empty) {
                            super.updateItem(hall, empty);
                            if (empty || hall == null) {
                                setText(null);
                            } else {
                                setText(hall.getName());
                            }
                        }
                    });
                    
                    // Set button cell to display selected hall name
                    hallComboBox.setButtonCell(new ListCell<Hall>() {
                        @Override
                        protected void updateItem(Hall hall, boolean empty) {
                            super.updateItem(hall, empty);
                            if (empty || hall == null) {
                                setText("Выберите зал");
                            } else {
                                setText(hall.getName());
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.error("Ошибка", "Не удалось загрузить список залов");
        }
    }
    
    private void clearFields() {
        selectedSession = null;
        movieComboBox.setValue(null);
        hallComboBox.setValue(null);
        startDatePicker.setValue(LocalDate.now());
        
        // Reset time to current time rounded to nearest 15 minutes
        LocalTime now = LocalTime.now();
        int currentMinute = now.getMinute();
        int roundedMinute = (currentMinute / 15) * 15;
        startHourCombo.getSelectionModel().select(Integer.valueOf(now.getHour()));
        startMinuteCombo.getSelectionModel().select(Integer.valueOf(roundedMinute));
        
        priceField.clear();
        errorLabel.setText("");
        // Reset time fields to default
        initializeTimeCombos();
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
        try {
            Loader.loadScene((Stage) rolesButton.getScene().getWindow(), StagePath.ADMIN_ROLES_MANAGEMENT);
        } catch (Exception e) {
            AlertUtil.error("Ошибка", "Не удалось загрузить экран управления ролями: " + e.getMessage());
        }
    }

    @FXML
    void onAddButton(ActionEvent event) {
        try {
            Movie movie = movieComboBox.getValue();
            Hall hall = hallComboBox.getValue();
            LocalDateTime startDateTime = getStartDateTime();
            LocalDateTime endDateTime = getEndDateTime();
            String priceStr = priceField.getText().trim();

            // Validation
            if (movie == null || hall == null || startDateTime == null || endDateTime == null || priceStr.isEmpty()) {
                errorLabel.setText("Все поля обязательны!");
                return;
            }

            if (endDateTime.isBefore(startDateTime)) {
                errorLabel.setText("Ошибка: Время окончания не может быть раньше времени начала!");
                return;
            }


            float price;
            try {
                price = Float.parseFloat(priceStr);
                if (price <= 0) {
                    errorLabel.setText("Цена должна быть положительной!");
                    return;
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Неверный формат цены!");
                return;
            }

            // Create new session
            Session newSession = new Session();
            newSession.setMovie(movie);
            newSession.setHall(hall);
            newSession.setStartTime(startDateTime);
            newSession.setEndTime(endDateTime);
            newSession.setPrice(price);

            // Save to server
            Response response = sessionService.createSession(newSession);
            if (response.isSuccess()) {
                AlertUtil.info("Успех", "Сеанс успешно добавлен");
                loadSessionsTable();
                clearFields();
            } else {
                errorLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onDeleteButton(ActionEvent event) {
        if (selectedSession == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите сеанс!");
            return;
        }

        if (AlertUtil.confirmation("Удаление сеанса", "Вы уверены, что хотите удалить сеанс?" +
                "\nЭто действие нельзя отменить.") != ButtonType.OK) {
            return;
        }

        Response response = sessionService.deleteSession(selectedSession.getId());
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Сеанс успешно удален");
            loadSessionsTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }

    @FXML
    void onRefreshButton(ActionEvent event) {
        if (selectedSession == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите сеанс!");
            return;
        }

        try {
            Movie movie = movieComboBox.getValue();
            Hall hall = hallComboBox.getValue();
            LocalDateTime startDateTime = getStartDateTime();
            LocalDateTime endDateTime = getEndDateTime();
            String priceStr = priceField.getText().trim();

            // Validation
            if (movie == null || hall == null || startDateTime == null || endDateTime == null || priceStr.isEmpty()) {
                errorLabel.setText("Все поля обязательны!");
                return;
            }

            if (endDateTime.isBefore(startDateTime)) {
                errorLabel.setText("Ошибка: Время окончания не может быть раньше времени начала!");
                return;
            }

            float price;
            try {
                price = Float.parseFloat(priceStr);
                if (price <= 0) {
                    errorLabel.setText("Цена должна быть положительной!");
                    return;
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Неверный формат цены!");
                return;
            }

            selectedSession.setMovie(movie);
            selectedSession.setHall(hall);
            selectedSession.setStartTime(startDateTime);
            selectedSession.setEndTime(endDateTime);
            selectedSession.setPrice(price);

            Response response = sessionService.updateSession(selectedSession);
            if (response.isSuccess()) {
                AlertUtil.info("Успех", "Сеанс успешно обновлен");
                loadSessionsTable();
                clearFields();
            } else {
                errorLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка ввода: " + e.getMessage());
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