package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.User;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;
import com.server.services.PersonService;
import com.server.services.RoleService;
import com.server.services.UserService;
import com.server.utils.Pair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

public class UserController {
    private final UserService userService;
    private final PersonService personService;
    private final RoleService roleService;
    private static UserController instance;

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public UserController(UserService userService, PersonService personService, RoleService roleService) {
        this.userService = userService;
        this.personService = personService;
        this.roleService = roleService;
    }

    public UserController() {
        this.userService = new UserService();
        this.personService = new PersonService();
        this.roleService = new RoleService();
    }

    public Response login(Request request) {
        Deserializer deserializer = new Deserializer();
        User user = (User) deserializer.extractData(request);

        try {
            //user.setPassword(hashPassword(user.getPassword()));
            User existingUser = userService.login(user);
            String loggedInUser = Serializer.toJson(existingUser);

            return new Response(true, "Login Successful", loggedInUser);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            return new Response(false, "An unexpected error occurred", null);
        }
    }


    public Response register(Request request) {
        Deserializer deserializer = new Deserializer();
        Object extractedData;

        try {
            extractedData = deserializer.extractData(request);
        } catch (IllegalArgumentException e) {
            return new Response(false, "Invalid user data", null);
        }

        if (!(extractedData instanceof User user)) {
            return new Response(false, "Invalid user data", null);
        }

        try {
            String registeredUserJson = Serializer.toJson(
                    userService.register(user, personService, roleService)
            );
            return new Response(true, "Registration Successful", registeredUserJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllUsers() {
        try {
            List<User> users = userService.findAllEntities();
            String usersJson = Serializer.toJson(users);
            return new Response(true, "Список пользователей получен", usersJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }


    public Response deleteUser(Request request) {
        Deserializer deserializer = new Deserializer();
        String login = (String) deserializer.extractData(request);

        try {
            User foundUser = userService.findByUsername(login);
            if (foundUser != null) {
                personService.deleteEntity(foundUser.getPerson());

                return new Response(true, "User deleted successfully", null);
            } else {
                return new Response(false, "User not found", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }


    public Response updateEntity(Request request) {
        Deserializer deserializer = new Deserializer();
        User userToUpdate = (User) deserializer.extractData(request);

        if (userToUpdate == null) {
            return new Response(false, "Неверные данные пользователя", null);
        }
        userToUpdate.setPassword(hashPassword(userToUpdate.getPassword()));
        try {
            userService.updateEntity(userToUpdate, personService);
            return new Response(true, "Пользователь успешно обновлен", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response readEntity(Request request) {
        Deserializer deserializer = new Deserializer();
        String username = (String) deserializer.extractData(request);

        try {
            User user = userService.findByUsername(username);
            String userJson = Serializer.toJson(user);
            if (user != null) {
                return new Response(true, "User retrieved successfully", userJson);
            } else {
                return new Response(false, "User not found", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }
}
