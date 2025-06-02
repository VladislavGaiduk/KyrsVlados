package com.gui.services;

import com.server.enums.Operation;
import com.server.models.Hall;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.serializer.Serializer;

public class HallService {
    
    public Response getAll() {
        return ServerClient.getInstance().sendRequest(new Request(Operation.GET_ALL_HALLS));
    }

    public Response addHall(Hall hall) {
        if (hall == null || hall.getName() == null || hall.getName().trim().isEmpty()) {
            return new Response(false, "Название зала не может быть пустым", null);
        }
        if (hall.getCapacity() == null || hall.getCapacity() <= 0) {
            return new Response(false, "Вместимость зала должна быть положительным числом", null);
        }
        Request request = new Request(Operation.CREATE_HALL, Serializer.toJson(hall));
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response updateHall(Hall hall) {
        if (hall == null || hall.getId() == null || hall.getName() == null || hall.getName().trim().isEmpty() || 
            hall.getCapacity() == null || hall.getCapacity() <= 0) {
            return new Response(false, "Некорректные данные зала", null);
        }
        Request request = new Request(Operation.UPDATE_HALL, Serializer.toJson(hall));
        Response response = ServerClient.getInstance().sendRequest(request);
        if (!response.isSuccess()) {
            return new Response(false, "Ошибка обновления зала: " + response.getMessage(), null);
        }
        return response;
    }

    public Response deleteHall(Hall hall) {
        if (hall == null || hall.getId() == null) {
            return new Response(false, "ИД зала обязателен", null);
        }
        Request request = new Request(Operation.DELETE_HALL, Serializer.toJson(hall));
        return ServerClient.getInstance().sendRequest(request);
    }
}
