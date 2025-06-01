package com.server.serializer;

import com.google.gson.reflect.TypeToken;
import com.server.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.server.network.Request;
import com.server.utils.Pair;




import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.server.models.*;
        import com.server.network.Request;
import com.server.utils.Pair;

public class Deserializer {
    public Object extractData(Request request) {
        Gson gson = new Gson();
        try {
            return switch (request.getOperation()) {
                case LOGIN,  REGISTER -> gson.fromJson(request.getData(), User.class);
                case DELETE_USER, READ_USER -> gson.fromJson(request.getData(), String.class);
                case UPDATE_USER -> gson.fromJson(request.getData(), User.class);
                case CREATE_GENRE,DELETE_GENRE,UPDATE_GENRE -> gson.fromJson(request.getData(), Genre.class);
                case CREATE_MOVIE, UPDATE_MOVIE, DELETE_MOVIE -> gson.fromJson(request.getData(), Movie.class);
                case GET_ALL_GENRES -> gson.fromJson(request.getData(), new TypeToken<Pair<Integer, Genre>>(){}.getType());
                case GET_ALL_MOVIES -> gson.fromJson(request.getData(), new TypeToken<Pair<Integer, Movie>>(){}.getType());
                case GET_ALL_USERS -> gson.fromJson(request.getData(), new TypeToken<Pair<Integer, User>>(){}.getType());
                case GET_ALL_ROLES -> gson.fromJson(request.getData(), new TypeToken<Pair<Integer, Role>>(){}.getType());
                case UPDATE_ROLE -> gson.fromJson(request.getData(), Role.class);
                case CREATE_ROLE -> gson.fromJson(request.getData(), Role.class);
                case DELETE_ROLE -> gson.fromJson(request.getData(), String.class);
                case DISCONNECT -> gson.fromJson(request.getData(), String.class);
                default -> null;
            };
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON string", e);
        }
    }
}