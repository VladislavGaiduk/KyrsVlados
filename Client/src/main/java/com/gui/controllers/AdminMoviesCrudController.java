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
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
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
    @FXML
    private ComboBox<Genre> genreFilterCombo;
    @FXML
    private TextField titleFilterField;
    @FXML
    private TextField yearFilterField;


    private MovieService movieService;
    private GenreService genreService;
    private Movie selectedMovie;
    private final ObservableList<Movie> allMovies = FXCollections.observableArrayList();
    private FilteredList<Movie> filteredMovies;


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
        Callback<ListView<Genre>, ListCell<Genre>> cellFactory = lv -> new ListCell<Genre>() {
            @Override
            protected void updateItem(Genre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        };
        
        genreComboBox.setCellFactory(cellFactory);
        genreComboBox.setButtonCell(cellFactory.call(null));
        
        // Setup filter combo box
        genreFilterCombo.setCellFactory(cellFactory);
        genreFilterCombo.setButtonCell(new ListCell<Genre>() {
            @Override
            protected void updateItem(Genre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Все жанры" : item.getName());
            }
        });

        // Инициализация фильтрации
        filteredMovies = new FilteredList<>(allMovies, p -> true);
        SortedList<Movie> sortedData = new SortedList<>(filteredMovies);
        sortedData.comparatorProperty().bind(moviesTable.comparatorProperty());
        moviesTable.setItems(sortedData);

        // Настройка слушателей фильтров
        setupFilterListeners();

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
            if (genres != null) {
                // Create "All Genres" option
                Genre allGenres = new Genre();
                allGenres.setName("Все жанры");
                allGenres.setId(-1);
                
                // Add to combo boxes
                ObservableList<Genre> genreList = FXCollections.observableArrayList(genres);
                genreComboBox.setItems(genreList);
                
                // For filter combo, add "All Genres" as first item
                ObservableList<Genre> filterGenres = FXCollections.observableArrayList(allGenres);
                filterGenres.addAll(genres);
                genreFilterCombo.setItems(filterGenres);
                genreFilterCombo.getSelectionModel().selectFirst();
            }
        } else {
            AlertUtil.error("Ошибка загрузки", "Не удалось загрузить жанры: " + response.getMessage());
        }
    }

    private void loadMoviesTable() {
        Response response = movieService.getAll();
        if (response.isSuccess()) {
            List<Movie> movies = new Deserializer().extractListData(response.getData(), Movie.class);
            if (movies != null) {
                allMovies.setAll(movies);
                applyFilters();
            }
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

    private void setupFilterListeners() {
        // Add listeners to filter fields
        titleFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        genreFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        yearFilterField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                yearFilterField.setText(newVal.replaceAll("[^\\d]", ""));
            } else {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        if (filteredMovies == null) return;
        
        filteredMovies.setPredicate(movie -> {
            if (movie == null) return false;
            
            // Filter by title
            String titleFilter = titleFilterField.getText().toLowerCase();
            if (!titleFilter.isEmpty() && 
                (movie.getTitle() == null || !movie.getTitle().toLowerCase().contains(titleFilter))) {
                return false;
            }
            
            // Filter by genre
            Genre selectedGenre = genreFilterCombo.getValue();
            if (selectedGenre != null && selectedGenre.getId() != -1 && 
                (movie.getGenre() == null || !selectedGenre.getId().equals(movie.getGenre().getId()))) {
                return false;
            }
            
            // Filter by year
            String yearFilter = yearFilterField.getText();
            if (!yearFilter.isEmpty()) {
                try {
                    int year = Integer.parseInt(yearFilter);
                    if (movie.getYear() != year) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // If year is not a valid number, don't filter by year
                }
            }
            
            return true;
        });
    }

    @FXML
    private void onResetFilters() {
        titleFilterField.clear();
        if (!genreFilterCombo.getItems().isEmpty()) {
            genreFilterCombo.getSelectionModel().select(0);
        }
        yearFilterField.clear();
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
    @FXML
    void onExitButton(ActionEvent event) {
        Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.MAIN_MENU);
    }
}
