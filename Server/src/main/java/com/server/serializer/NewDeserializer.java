package com.server.serializer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.server.enums.Operation;
import com.server.models.*;
import com.server.network.Request;
import com.server.utils.Pair;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class NewDeserializer {
    public Object extractData(Request request) {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
        try {
            Operation operation = request.getOperation();
            String data = request.getData();
            
            if (operation == null) {
                throw new IllegalArgumentException("Operation cannot be null");
            }
            
            return switch (operation) {
                // User operations
                case LOGIN, REGISTER, UPDATE_USER -> 
                    gson.fromJson(data, User.class);
                    
                case DELETE_USER, READ_USER -> 
                    data;  // Just return the string data (ID)
                    
                case GET_ALL_USERS -> 
                    gson.fromJson(data, new TypeToken<Pair<Integer, User>>(){}.getType());
                    
                // Role operations
                case GET_ALL_ROLES -> 
                    gson.fromJson(data, new TypeToken<Pair<Integer, Role>>(){}.getType());
                    
                case CREATE_ROLE, UPDATE_ROLE -> 
                    gson.fromJson(data, Role.class);
                    
                case DELETE_ROLE -> 
                    data;  // Just return the string data (ID)
                    
                // Movie operations
                case CREATE_MOVIE, UPDATE_MOVIE, DELETE_MOVIE -> 
                    gson.fromJson(data, Movie.class);
                    
                case GET_ALL_MOVIES -> 
                    gson.fromJson(data, new TypeToken<Pair<Integer, Movie>>(){}.getType());
                    
                // Genre operations
                case CREATE_GENRE, DELETE_GENRE, UPDATE_GENRE -> 
                    gson.fromJson(data, Genre.class);
                    
                case GET_ALL_GENRES -> 
                    gson.fromJson(data, new TypeToken<Pair<Integer, Genre>>(){}.getType());
                    
                // Hall operations
                case CREATE_HALL, UPDATE_HALL -> 
                    gson.fromJson(data, Hall.class);
                    
                case DISCONNECT, DELETE_HALL -> 
                    data;  // Just return the string data (ID)
                    
                // Session operations
                case GET_ALL_SESSIONS, CREATE_SESSION, UPDATE_SESSION, DELETE_SESSION -> 
                    gson.fromJson(data, Session.class);
                    
                case GET_SESSIONS_BY_DATE_RANGE -> 
                    gson.fromJson(data, String[].class);
                    
                // Ticket operations
                case GET_ALL_TICKETS, GET_TICKETS_BY_SESSION, GET_TICKETS_BY_USER,
                     CREATE_TICKET, UPDATE_TICKET -> 
                    gson.fromJson(data, Ticket.class);

                case DELETE_TICKET ->
                    gson.fromJson(data, Integer.class);
                    
                default -> 
                    null;
            };
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON string: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing data: " + e.getMessage(), e);
        }
    }
}
