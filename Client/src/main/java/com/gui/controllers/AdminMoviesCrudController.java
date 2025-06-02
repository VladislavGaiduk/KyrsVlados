package com.gui.controllers;

import com.gui.services.GenreService;
import com.gui.services.MovieService;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.gui.enums.StagePath;
import com.server.models.Genre;
import com.server.models.Movie;
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

public class AdminMoviesCrudController implements Initializable {
    @FXML
    private Button openListButton;
    @FXML
    private AnchorPane hiddenBox;
    @FXML
    private Button moviesButton;
    @FXML
    private Button usersButton;
    @FXML
    private TableView<Movie> moviesTable;
    @FXML
    private TableColumn<Movie, Integer> idColumn;
    @FXML
    private TableColumn<Movie, String> titleColumn;
    @FXML
    private TableColumn<Movie, String> genreColumn;
    @FXML
    private TableColumn<Movie, Double> ratingColumn;
    @FXML
    private TableColumn<Movie, Integer> yearColumn;
    @FXML
    private TableColumn<Movie, Integer> durationColumn;
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<Genre> genreComboBox;
    @FXML
    private TextField ratingField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField durationField;
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

    private MovieService movieService;
    private GenreService genreService;
    private Movie selectedMovie;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        movieService = new MovieService();
        genreService = new GenreService();
        hiddenBox.setVisible(false);

        moviesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Настройка колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            return new SimpleStringProperty(movie.getGenre() != null ? movie.getGenre().getName() : "");
        });
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        // Настройка ComboBox для отображения только названия жанра
        genreComboBox.setCellFactory(param -> new ListCell<Genre>() {
            @Override
            protected void updateItem(Genre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        genreComboBox.setButtonCell(new ListCell<Genre>() {
            @Override
            protected void updateItem(Genre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // Загрузка жанров и фильмов
        loadGenres();
        loadMoviesTable();

        // Настройка слушателя выбора в таблице
        setupTableSelectionObserver();
    }

    private void loadGenres() {
        Response response = genreService.getAll();
        if (response.isSuccess()) {
            List<Genre> genres = new Deserializer().extractListData(response.getData(), Genre.class);
            genreComboBox.setItems(FXCollections.observableArrayList(genres));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить жанры: " + response.getMessage());
        }
    }

    private void loadMoviesTable() {
        moviesTable.getItems().clear();
        Response response = movieService.getAll();
        if (response.isSuccess()) {
            List<Movie> movies = new Deserializer().extractListData(response.getData(), Movie.class);
            moviesTable.setItems(FXCollections.observableArrayList(movies));
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить фильмы: " + response.getMessage());
        }
    }

    private void setupTableSelectionObserver() {
        moviesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMovie = newSelection;
                titleField.setText(selectedMovie.getTitle());
                genreComboBox.setValue(selectedMovie.getGenre());
                ratingField.setText(String.valueOf(selectedMovie.getRating()));
                yearField.setText(String.valueOf(selectedMovie.getYear()));
                durationField.setText(String.valueOf(selectedMovie.getDurationMinutes()));
                errorLabel.setText("");
            } else {
                clearFields();
            }
        });
    }

    private void clearFields() {
        titleField.clear();
        genreComboBox.setValue(null);
        ratingField.clear();
        yearField.clear();
        durationField.clear();
        errorLabel.setText("");
        selectedMovie = null;
    }

    @FXML
    void onAddButton(ActionEvent event) {
        String title = titleField.getText().trim();
        String ratingText = ratingField.getText().trim();
        String yearText = yearField.getText().trim();
        Genre selectedGenre = genreComboBox.getValue();
        String durationText = durationField.getText().trim();


        if (title.isEmpty() || ratingText.isEmpty() || yearText.isEmpty() || selectedGenre == null || durationText.isEmpty()) {
            errorLabel.setText("Все поля должны быть заполнены!");
            return;
        }

        try {
            float rating = Float.parseFloat(ratingText);
            int year = Integer.parseInt(yearText);
            int currentYear = java.time.Year.now().getValue();
            int duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                errorLabel.setText("Длительность должна быть больше 0 минут");
                return;
            }
            if (rating < 0 || rating > 10) {
                errorLabel.setText("Рейтинг должен быть от 0 до 10!");
                return;
            }
            if (year < 1888 || year > currentYear) {
                errorLabel.setText("Год должен быть от 1888 до " + currentYear + "!");
                return;
            }

            Movie newMovie = new Movie();
            newMovie.setTitle(title);
            newMovie.setGenre(selectedGenre);
            newMovie.setRating(rating);
            newMovie.setYear(year);
            newMovie.setDurationMinutes(duration);
            Response response = movieService.addMovie(newMovie);
            if (response.isSuccess()) {
                AlertUtil.info("Успех", "Фильм успешно добавлен");
                loadMoviesTable();
                clearFields();
            } else {
                errorLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Рейтинг и год должны быть числами!");
        }
    }

    @FXML
    void onDeleteButton(ActionEvent event) {
        if (selectedMovie == null) {
            AlertUtil.warning("Предупреждение", "Сначала выберите фильм!");
            return;
        }

        if (AlertUtil.confirmation("Удаление фильма", "Вы уверены, что хотите удалить фильм?") != ButtonType.OK) {
            return;
        }

        Response response = movieService.deleteMovie(selectedMovie);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Фильм успешно удален");
            loadMoviesTable();
            clearFields();
        } else {
            errorLabel.setText("Ошибка: " + response.getMessage());
        }
    }


    @FXML
    void onRefreshButton(ActionEvent event) {
        if (selectedMovie == null) {
            errorLabel.setText("Сначала выберите фильм для обновления!");
            return;
        }

        String title = titleField.getText().trim();
        String ratingText = ratingField.getText().trim();
        String yearText = yearField.getText().trim();
        Genre selectedGenre = genreComboBox.getValue();
        String durationText = durationField.getText().trim();

        if (title.isEmpty() || ratingText.isEmpty() || yearText.isEmpty() || selectedGenre == null || durationText.isEmpty()) {
            errorLabel.setText("Все поля должны быть заполнены!");
            return;
        }

        try {
            float rating = Float.parseFloat(ratingText);
            int year = Integer.parseInt(yearText);
            int currentYear = java.time.Year.now().getValue();
            int duration = Integer.parseInt(durationText);
            
            if (duration <= 0) {
                errorLabel.setText("Длительность должна быть больше 0 минут");
                return;
            }
            if (rating < 0 || rating > 10) {
                errorLabel.setText("Рейтинг должен быть от 0 до 10!");
                return;
            }
            if (year < 1888 || year > currentYear) {
                errorLabel.setText("Год должен быть от 1888 до " + currentYear + "!");
                return;
            }

            // Update the selected movie with new values
            selectedMovie.setTitle(title);
            selectedMovie.setGenre(selectedGenre);
            selectedMovie.setRating(rating);
            selectedMovie.setYear(year);
            selectedMovie.setDurationMinutes(duration);

            Response response = movieService.updateMovie(selectedMovie);
            if (response.isSuccess()) {
                AlertUtil.info("Успех", "Данные фильма успешно обновлены");
                loadMoviesTable();
                clearFields();
            } else {
                errorLabel.setText("Ошибка: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Рейтинг и год должны быть числами!");
        }
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
