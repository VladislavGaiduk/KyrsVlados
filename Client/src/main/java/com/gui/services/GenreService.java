// GenreService.java
package com.gui.services;

import com.server.enums.Operation;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.models.Genre;
import com.server.serializer.Serializer;

public class GenreService {
    public Response getAll() {
        return ServerClient.getInstance().sendRequest(new Request(Operation.GET_ALL_GENRES));
    }

    public Response addGenre(Genre genre) {
        if (genre == null || genre.getName() == null || genre.getName().trim().isEmpty()) {
            return new Response(false, "Название жанра не может быть пустым", null);
        }
        Request request = new Request(Operation.CREATE_GENRE, Serializer.toJson(genre));
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response updateGenre(Genre genre) {
        if (genre == null || genre.getId() == null || genre.getName() == null || genre.getName().trim().isEmpty()) {
            return new Response(false, "ИД и название жанра обязательны", null);
        }
        Request request = new Request(Operation.UPDATE_GENRE, Serializer.toJson(genre));
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response deleteGenre(Genre genre) {
        if (genre == null || genre.getId() == null) {
            return new Response(false, "ИД жанра обязателен", null);
        }
        Request request = new Request(Operation.DELETE_GENRE, Serializer.toJson(genre));
        return ServerClient.getInstance().sendRequest(request);
    }
}