package com.gui.services;

import com.server.enums.Operation;
import com.server.models.Person;
import com.server.models.Role;
import com.server.models.User;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.serializer.Serializer;
import com.server.utils.Pair;

public class UserService {
    public Response login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return new Response(false, "Имя пользователя и пароль обязательны", null);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        String userJson = Serializer.toJson(user);
        Request request = new Request(Operation.LOGIN, userJson);
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response addUser(String username, String password, String firstName, String lastName, String patronymic, Role role) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() ||
                firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty() ||
                role == null || role.getId() == null) {
            return new Response(false, "Некорректные данные: имя пользователя, пароль, имя, фамилия и роль обязательны", null);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPatronomic(patronymic != null ? patronymic.trim() : null);
        user.setPerson(person);
        String userJson = Serializer.toJson(user);
        Request request = new Request(Operation.REGISTER, userJson);
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response getAll() {
        return ServerClient.getInstance().sendRequest(new Request(Operation.GET_ALL_USERS));
    }

    public Response delUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new Response(false, "Имя пользователя обязательно", null);
        }
        return ServerClient.getInstance().sendRequest(new Request(Operation.DELETE_USER, Serializer.toJson(username)));
    }
    public Response register(String username, String password, String firstName, String lastName, String patronymic, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        if(role == null) {
            role = new Role();
            role.setName("user");
        }
        user.setRole(role);

        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPatronomic(patronymic);
        user.setPerson(person);

        String userJson = Serializer.toJson(user);
        Request request = new Request(Operation.REGISTER, userJson);

        return ServerClient.getInstance().sendRequest(request);
    }
    public Response updateUser(User user) {
        if (user == null || user.getId() == null || user.getUsername() == null || user.getUsername().trim().isEmpty() ||
                user.getPassword() == null || user.getPassword().trim().isEmpty() ||
                user.getRole() == null || user.getPerson() == null) {
            return new Response(false, "Некорректные данные пользователя: ИД, имя, пароль, роль и персона обязательны", null);
        }
        return ServerClient.getInstance().sendRequest(new Request(Operation.UPDATE_USER, Serializer.toJson(user)));
    }
}