package com.gui.controllers;

import com.gui.enums.StagePath;
import com.gui.services.*;
import com.gui.utils.AlertUtil;
import com.gui.utils.Loader;
import com.server.models.*;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminTicketsCrudController implements Initializable {
    @FXML private Button openListButton;
    @FXML private AnchorPane hiddenBox;
    @FXML private Button moviesButton;
    @FXML private Button usersButton;
    @FXML private Button genresButton;
    @FXML private Button rolesButton;
    @FXML private Button hallsButton;
    @FXML private Button sessionsButton;
    @FXML private Button ticketsButton;
    
    // Filter controls
    @FXML private ComboBox<Session> sessionFilterCombo;
    @FXML private ComboBox<User> userFilterCombo;
    @FXML private TextField seatFilterField;
    
    // Table and columns
    @FXML private TableView<Ticket> ticketsTable;
    @FXML private TableColumn<Ticket, Integer> idColumn;
    @FXML private TableColumn<Ticket, String> sessionColumn;
    @FXML private TableColumn<Ticket, String> userColumn;
    @FXML private TableColumn<Ticket, Integer> seatColumn;
    @FXML private TableColumn<Ticket, String> purchaseTimeColumn;
    
    // Form controls
    @FXML private AnchorPane ticketDataBox;
    @FXML private ComboBox<Session> sessionCombo;
    @FXML private ComboBox<User> userCombo;
    @FXML private TextField seatField;
    @FXML private Label errorLabel;
    
    // Buttons
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    
    // Services
    private final TicketService ticketService = new TicketService();
    private final SessionService sessionService = new SessionService();
    private final UserService userService = new UserService();
    
    // Data
    private ObservableList<Ticket> allTickets = FXCollections.observableArrayList();
    private FilteredList<Ticket> filteredTickets = new FilteredList<>(allTickets);
    private ObservableList<Session> allSessions = FXCollections.observableArrayList();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    
    // Formatters
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupCellValueFactories();
        setupTableSelectionListener();
        setupFilterListeners();
        loadInitialData();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        seatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        
        // Custom cell value factories for complex objects
        sessionColumn.setCellValueFactory(cellData -> {
            Session session = cellData.getValue().getSession();
            if (session != null && session.getMovie() != null) {
                return new SimpleStringProperty(
                    String.format("%s (%s)", 
                        session.getMovie().getTitle(),
                        session.getStartTime().format(dateTimeFormatter)
                    )
                );
            }
            return new SimpleStringProperty("");
        });
        
        userColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            if (user != null && user.getPerson() != null) {
                return new SimpleStringProperty(
                    String.format("%s %s", 
                        user.getPerson().getLastName(),
                        user.getPerson().getFirstName()
                    )
                );
            } else if (user != null) {
                return new SimpleStringProperty(user.getUsername());
            }
            return new SimpleStringProperty("");
        });
        
        purchaseTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime time = cellData.getValue().getPurchaseTime();
            return new SimpleStringProperty(time != null ? time.format(dateTimeFormatter) : "");
        });
    }
    
    private void setupCellValueFactories() {
        // Set up session combo box cell factory
        Callback<ListView<Session>, ListCell<Session>> sessionCellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Session session, boolean empty) {
                super.updateItem(session, empty);
                if (session == null || empty) {
                    setText("");
                } else {
                    setText(String.format("%s - %s | %s", 
                        session.getMovie().getTitle(),
                        session.getStartTime().format(dateTimeFormatter),
                        session.getHall().getName()
                    ));
                }
            }
        };
        
        sessionCombo.setCellFactory(sessionCellFactory);
        sessionCombo.setButtonCell(sessionCellFactory.call(null));
        
        // Set up user combo box cell factory
        Callback<ListView<User>, ListCell<User>> userCellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (user == null || empty) {
                    setText("");
                } else {
                    if (user.getPerson() != null) {
                        setText(String.format("%s %s (%s)", 
                            user.getPerson().getLastName(),
                            user.getPerson().getFirstName(),
                            user.getUsername()
                        ));
                    } else {
                        setText(user.getUsername());
                    }
                }
            }
        };
        
        userCombo.setCellFactory(userCellFactory);
        userCombo.setButtonCell(userCellFactory.call(null));
        
        // Set up filter combo boxes
        sessionFilterCombo.setCellFactory(sessionCellFactory);
        sessionFilterCombo.setButtonCell(sessionCellFactory.call(null));
        
        userFilterCombo.setCellFactory(userCellFactory);
        userFilterCombo.setButtonCell(userCellFactory.call(null));
    }
    
    private void setupTableSelectionListener() {
        ticketsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
                addButton.setText("Обновить");
                deleteButton.setDisable(false);
            } else {
                clearForm();
                addButton.setText("Добавить");
                deleteButton.setDisable(true);
            }
        });
    }
    
    private void setupFilterListeners() {
        // Add listeners to filter controls
        sessionFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        userFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        seatFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void loadInitialData() {
        // Load tickets
        new Thread(() -> {
            Response response = ticketService.getAllTickets();
            if (response != null && response.isSuccess()) {
                List<Ticket> tickets = new Deserializer().extractListData(response.getData(), Ticket.class);
                Platform.runLater(() -> {
                    allTickets.setAll(tickets);
                    ticketsTable.setItems(filteredTickets);
                });
            } else {
                Platform.runLater(() -> 
                    showError("Ошибка загрузки билетов: " + (response != null ? response.getMessage() : "Неизвестная ошибка"))
                );
            }
            
            // Load sessions
            Response sessionsResponse = sessionService.getAllSessions();
            if (sessionsResponse != null && sessionsResponse.isSuccess()) {
                List<Session> sessions = new Deserializer().extractListData(sessionsResponse.getData(), Session.class);
                Platform.runLater(() -> {
                    allSessions.setAll(sessions);
                    sessionCombo.setItems(allSessions);
                    sessionFilterCombo.setItems(FXCollections.observableArrayList(allSessions));
                });
            }
            
            // Load users
            Response usersResponse = userService.getAll();
            if (usersResponse != null && usersResponse.isSuccess()) {
                List<User> users = new Deserializer().extractListData(usersResponse.getData(), User.class);
                Platform.runLater(() -> {
                    allUsers.setAll(users);
                    userCombo.setItems(allUsers);
                    userFilterCombo.setItems(FXCollections.observableArrayList(allUsers));
                });
            }
        }).start();
    }
    
    private void applyFilters() {
        if (allTickets == null || allTickets.isEmpty()) {
            return;
        }
        
        Session selectedSession = sessionFilterCombo.getValue();
        User selectedUser = userFilterCombo.getValue();
        String seatFilter = seatFilterField.getText().trim();
        
        filteredTickets.setPredicate(ticket -> {
            if (ticket == null) return false;
            
            // Filter by session
            if (selectedSession != null && 
                (ticket.getSession() == null || !selectedSession.getId().equals(ticket.getSession().getId()))) {
                return false;
            }
            
            // Filter by user
            if (selectedUser != null && 
                (ticket.getUser() == null || !selectedUser.getId().equals(ticket.getUser().getId()))) {
                return false;
            }
            
            // Filter by seat number
            if (!seatFilter.isEmpty() && 
                (ticket.getSeatNumber() == null || 
                 !String.valueOf(ticket.getSeatNumber()).contains(seatFilter))) {
                return false;
            }
            
            return true;
        });
    }
    
    private void populateForm(Ticket ticket) {
        if (ticket == null) return;
        
        // Find and select the session in the combo box
        if (ticket.getSession() != null) {
            sessionCombo.getSelectionModel().select(
                allSessions.stream()
                    .filter(s -> s.getId().equals(ticket.getSession().getId()))
                    .findFirst()
                    .orElse(null)
            );
        }
        
        // Find and select the user in the combo box
        if (ticket.getUser() != null) {
            userCombo.getSelectionModel().select(
                allUsers.stream()
                    .filter(u -> u.getId().equals(ticket.getUser().getId()))
                    .findFirst()
                    .orElse(null)
            );
        }
        
        seatField.setText(String.valueOf(ticket.getSeatNumber()));
    }
    
    private void clearForm() {
        sessionCombo.getSelectionModel().clearSelection();
        userCombo.getSelectionModel().clearSelection();
        seatField.clear();
        errorLabel.setText("");
    }
    
    @FXML
    private void onAddButton(ActionEvent event) {
        Ticket selectedTicket = ticketsTable.getSelectionModel().getSelectedItem();
        
        // Validate input
        if (sessionCombo.getValue() == null) {
            showError("Выберите сеанс");
            return;
        }
        
        if (userCombo.getValue() == null) {
            showError("Выберите пользователя");
            return;
        }
        
        if (seatField.getText().trim().isEmpty()) {
            showError("Введите номер места");
            return;
        }
        
        try {
            int seatNumber = Integer.parseInt(seatField.getText().trim());
            if (seatNumber <= 0) {
                throw new NumberFormatException();
            }
            
            Ticket ticket = selectedTicket != null ? selectedTicket : new Ticket();
            ticket.setSession(sessionCombo.getValue());
            ticket.setUser(userCombo.getValue());
            ticket.setSeatNumber(seatNumber);
            
            // Check if seat is already taken for this session
            boolean seatTaken = allTickets.stream()
                .filter(t -> t.getSession().getId().equals(ticket.getSession().getId()))
                .filter(t -> t.getSeatNumber().equals(ticket.getSeatNumber()))
                .anyMatch(t -> selectedTicket == null || !t.getId().equals(selectedTicket.getId()));
                
            if (seatTaken) {
                showError("Это место уже занято на выбранном сеансе");
                return;
            }
            

                Response response = selectedTicket == null ? 
                    ticketService.createTicket(ticket) : 
                    ticketService.updateTicket(ticket);
                    

                    if (response != null && response.isSuccess()) {
                        loadInitialData();
                        clearForm();
                        ticketsTable.getSelectionModel().clearSelection();
                        showSuccess(selectedTicket == null ? "Билет успешно добавлен" : "Билет успешно обновлен");
                    } else {
                        showError("Ошибка при сохранении билета: " + 
                            (response != null ? response.getMessage() : "Неизвестная ошибка"));
                    }


            
        } catch (NumberFormatException e) {
            showError("Некорректный номер места. Введите положительное число");
        }
    }
    
    @FXML
    private void onDeleteButton(ActionEvent event) {
        Ticket selectedTicket = ticketsTable.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showError("Выберите билет для удаления");
            return;
        }
        
        if (AlertUtil.confirmation("Подтверждение", "Вы уверены, что хотите удалить выбранный билет?") != ButtonType.OK) {
            return;
        }
        

        Response response = ticketService.deleteTicket(selectedTicket.getId());
                if (response != null && response.isSuccess()) {
                    loadInitialData();
                    clearForm();
                    showSuccess("Билет успешно удален");
                } else {
                    showError("Ошибка при удалении билета: " + 
                        (response != null ? response.getMessage() : "Неизвестная ошибка"));
                }

    }
    
    @FXML
    private void onRefreshButton(ActionEvent event) {
        loadInitialData();
        clearForm();
        ticketsTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void onResetFilters(ActionEvent event) {
        sessionFilterCombo.getSelectionModel().clearSelection();
        userFilterCombo.getSelectionModel().clearSelection();
        seatFilterField.clear();
    }
    
    // Navigation methods
    @FXML
    private void onOpenListButton(ActionEvent event) {
        hiddenBox.setVisible(!hiddenBox.isVisible());
    }
    
    @FXML
    private void onMoviesButton(ActionEvent event) {
        Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.ADMIN_MOVIES_MANAGMENT);
    }
    
    @FXML
    private void onUsersButton(ActionEvent event) {
        Loader.loadScene((Stage) usersButton.getScene().getWindow(), StagePath.ADMIN_USERS_MANAGEMENT);
    }
    
    @FXML
    private void onGenresButton(ActionEvent event) {
        Loader.loadScene((Stage) genresButton.getScene().getWindow(), StagePath.ADMIN_GENRES_MANAGEMENT);
    }
    
    @FXML
    private void onRolesButton(ActionEvent event) {
        Loader.loadScene((Stage) rolesButton.getScene().getWindow(), StagePath.ADMIN_ROLES_MANAGEMENT);
    }
    
    @FXML
    private void onHallsButton(ActionEvent event) {
        Loader.loadScene((Stage) hallsButton.getScene().getWindow(), StagePath.ADMIN_HALLS_MANAGEMENT);
    }
    
    @FXML
    private void onSessionsButton(ActionEvent event) {
        Loader.loadScene((Stage) sessionsButton.getScene().getWindow(), StagePath.ADMIN_SESSIONS_MANAGEMENT);
    }
    
    @FXML
    private void onTicketsButton(ActionEvent event) {
        // Already on tickets page
    }
    
    // Helper methods
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");
    }
    
    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #388e3c;");
    }
    @FXML
    void onExitButton(ActionEvent event) {
        Loader.loadScene((Stage) moviesButton.getScene().getWindow(), StagePath.MAIN_MENU);
    }
}
