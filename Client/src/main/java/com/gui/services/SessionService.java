package com.gui.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gui.utils.LocalDateTimeAdapter;
import com.server.enums.Operation;
import com.server.models.Session;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionService {
    private final Gson gson;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SessionService() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    }

    public Response getAllSessions() {
        try {
            Request request = new Request(Operation.GET_ALL_SESSIONS);
            Response response = ServerClient.getInstance().sendRequest(request);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Session> getSessionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String[] dates = {
                startDate.format(DATE_TIME_FORMATTER),
                endDate.format(DATE_TIME_FORMATTER)
            };
            
            Request request = new Request(
                Operation.GET_SESSIONS_BY_DATE_RANGE,
                gson.toJson(dates)
            );
            
            Response response = ServerClient.getInstance().sendRequest(request);
            
            if (response.isSuccess()) {
                Type listType = new TypeToken<ArrayList<Session>>(){}.getType();
                return gson.fromJson(response.getData(), listType);
            } else {
                System.err.println("Failed to get sessions by date range: " + response.getMessage());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Response createSession(Session session) {
        try {
            String sessionJson = gson.toJson(session);
            System.out.println("Sending session data: " + sessionJson);
            
            Request request = new Request(
                Operation.CREATE_SESSION,
                sessionJson
            );
            
            Response response = ServerClient.getInstance().sendRequest(request);
            
            if (response == null) {
                System.err.println("Failed to get response from server");
                return new Response(false, "Не удалось подключиться к серверу",null);
            }
            
            if (!response.isSuccess()) {
                System.err.println("Failed to create session: " + response.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при создании сеанса: " + e.getMessage(),null);
        }
    }

    public Response updateSession(Session session) {
        try {
            Request request = new Request(
                Operation.UPDATE_SESSION,
                gson.toJson(session)
            );
            
            Response response = ServerClient.getInstance().sendRequest(request);
            
            if (!response.isSuccess()) {
                System.err.println("Failed to update session: " + response.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response deleteSession(int sessionId) {
        try {
            Session session = new Session();
            session.setId(sessionId);
            
            Request request = new Request(
                Operation.DELETE_SESSION,
                gson.toJson(session)
            );
            
            Response response = ServerClient.getInstance().sendRequest(request);
            
            if (!response.isSuccess()) {
                System.err.println("Failed to delete session: " + response.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
