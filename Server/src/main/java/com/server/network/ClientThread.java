package com.server.network;


import com.server.controllers.GenreController;
import com.server.controllers.MovieController;
import com.server.controllers.RoleController;
import com.server.controllers.UserController;
import com.server.enums.Operation;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThread implements Runnable {
    private final Socket clientSocket;
    private final UserController userController;
    private final GenreController genreController; // Добавляем GenreController
    private final MovieController movieController;
    private final RoleController roleController;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.userController = new UserController();
        this.genreController = new GenreController();
        this.movieController = new MovieController();
        this.roleController = new RoleController();
    }

    @Override
    public void run() {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

            boolean keepRunning = true;
            while (keepRunning) {
                // Читаем запрос от клиента
                Request request = (Request) input.readObject();
                if (request != null) {
                    // Обрабатываем запрос и формируем ответ
                    Response response = processRequest(request);
                    if (request.getOperation() == Operation.DISCONNECT) {
                        keepRunning = false;
                    }
                    // Отправляем ответ клиенту
                    output.writeObject(response);
                    output.flush();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private Response processRequest(Request request) {
        try {
            return switch (request.getOperation()) {
                case LOGIN -> userController.login(request);
                case REGISTER -> userController.register(request);
                case UPDATE_USER -> userController.updateEntity(request);
                case DELETE_USER -> userController.deleteUser(request);
                case GET_ALL_USERS -> userController.getAllUsers();
                
                // Role operations
                case GET_ALL_ROLES -> roleController.getAllRoles();
                case CREATE_ROLE -> roleController.createRole(request);
                case UPDATE_ROLE -> roleController.updateRole(request);
                case DELETE_ROLE -> roleController.deleteRole(request);

                // Movie operations
                case CREATE_MOVIE -> movieController.createMovie(request);
                case UPDATE_MOVIE -> movieController.updateMovie(request);
                case DELETE_MOVIE -> movieController.deleteMovie(request);
                case GET_ALL_MOVIES -> movieController.getAllMovies();

                // Genre operations
                case CREATE_GENRE -> genreController.createGenre(request);
                case UPDATE_GENRE -> genreController.updateGenre(request);
                case DELETE_GENRE -> genreController.deleteGenre(request);
                case GET_ALL_GENRES -> genreController.getAllGenres();
                
                case DISCONNECT -> new Response(true, "Отключение успешно", null);
                default -> new Response(false, "Неизвестная операция", null);
            };
        } catch (Exception e) {
            return new Response(false, "Ошибка на сервере: " + e.getMessage(), null);
        }
    }

    private void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }
}


