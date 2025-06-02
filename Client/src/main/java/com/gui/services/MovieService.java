package com.gui.services;

import com.server.enums.Operation;
import com.server.models.Movie;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.serializer.Serializer;

import java.math.BigDecimal;

public class MovieService {
    public Response getAll() {
        return ServerClient.getInstance().sendRequest(new Request(Operation.GET_ALL_MOVIES));
    }

    public Response addMovie(Movie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().trim().isEmpty() ||
                movie.getRating() < 0 || movie.getRating() > 10 ||
                movie.getYear() < 1888 || movie.getYear() > java.time.Year.now().getValue() ||
                movie.getGenre() == null || movie.getGenre().getId() == null ||
                movie.getDurationMinutes() <= 0) {
            return new Response(false, "Некорректные данные фильма: проверьте название, рейтинг (0-10), год (1888-" +
                    java.time.Year.now().getValue() + "), длительность (>0) и жанр", null);
        }
        Request request = new Request(Operation.CREATE_MOVIE, Serializer.toJson(movie));
        return ServerClient.getInstance().sendRequest(request);
    }

    public Response updateMovie(Movie movie) {
        if (movie == null || movie.getId() == null || movie.getTitle() == null || movie.getTitle().trim().isEmpty() ||
                movie.getRating() < 0 || movie.getRating() > 10 ||
                movie.getYear() < 1888 || movie.getYear() > java.time.Year.now().getValue() ||
                movie.getGenre() == null || movie.getGenre().getId() == null) {
            return new Response(false, "Некорректные данные фильма: проверьте ИД, название, рейтинг (0-10), год (1888-" + java.time.Year.now().getValue() + ") и жанр", null);
        }
        Request request = new Request(Operation.UPDATE_MOVIE, Serializer.toJson(movie));
        Response response = ServerClient.getInstance().sendRequest(request);
        if (!response.isSuccess()) {
            return new Response(false, "Ошибка обновления фильма: " + response.getMessage(), null);
        }
        return response;
    }

    public Response deleteMovie(Movie movie) {
        if (movie == null || movie.getId() == null) {
            return new Response(false, "ИД фильма обязателен", null);
        }
        Request request = new Request(Operation.DELETE_MOVIE, Serializer.toJson(movie));
        return ServerClient.getInstance().sendRequest(request);
    }
}