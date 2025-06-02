package com.gui.services;

import com.server.enums.Operation;
import com.server.models.Role;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.serializer.Serializer;

public class RoleService {
    
    public Response getAll() {
        return ServerClient.getInstance().sendRequest(new Request(Operation.GET_ALL_ROLES));
    }

    public Response addRole(Role role) {
        if (role == null || role.getName() == null || role.getName().trim().isEmpty()) {
            return new Response(false, "Название роли не может быть пустым", null);
        }
        Request request = new Request(Operation.CREATE_ROLE, Serializer.toJson(role));
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response updateRole(Role role) {
        if (role == null || role.getId() == null || role.getName() == null || role.getName().trim().isEmpty()) {
            return new Response(false, "ИД и название роли обязательны", null);
        }
        Request request = new Request(Operation.UPDATE_ROLE, Serializer.toJson(role));
        Response response = ServerClient.getInstance().sendRequest(request);
        if (!response.isSuccess()) {
            return new Response(false, "Ошибка обновления роли: " + response.getMessage(), null);
        }
        return response;
    }

    public Response deleteRole(Role role) {
        if (role == null || role.getId() == null) {
            return new Response(false, "ИД роли обязателен", null);
        }
        // Send only the role ID as a string
        Request request = new Request(Operation.DELETE_ROLE, String.valueOf(role.getId()));
        return ServerClient.getInstance().sendRequest(request);
    }
}
