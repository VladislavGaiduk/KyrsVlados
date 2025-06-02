package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.Session;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;
import com.server.services.SessionService;

import java.time.LocalDateTime;
import java.util.List;

public class SessionController {
    private final SessionService sessionService;

    public SessionController() {
        this.sessionService = new SessionService();
    }

    public Response getAllSessions() {
        try {
            return new Response(true, "Sessions retrieved successfully", 
                Serializer.toJson(sessionService.findAllEntities()));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve sessions", null);
        }
    }

    public Response getSessionsByDateRange(Request request) {
        try {
            Deserializer deserializer = new Deserializer();
            String[] dates = (String[]) deserializer.extractData(request);
            
            if (dates == null || dates.length != 2) {
                return new Response(false, "Invalid date range format", null);
            }
            
            LocalDateTime startDate = LocalDateTime.parse(dates[0]);
            LocalDateTime endDate = LocalDateTime.parse(dates[1]);
            
            List<Session> sessions = sessionService.findSessionsByDateRange(startDate, endDate);
            return new Response(true, "Sessions retrieved successfully", 
                Serializer.toJson(sessions));
                
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve sessions by date range", null);
        }
    }

    public Response createSession(Request request) {
        try {
            Session session = (Session) new Deserializer().extractData(request);
            if (session == null) {
                return new Response(false, "Invalid session data", null);
            }
            
            Session createdSession = sessionService.createSession(session);
            return new Response(true, "Session created successfully", 
                Serializer.toJson(createdSession));
                
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to create session", null);
        }
    }

    public Response updateSession(Request request) {
        try {
            Session session = (Session) new Deserializer().extractData(request);
            if (session == null) {
                return new Response(false, "Invalid session data", null);
            }
            
            Session updatedSession = sessionService.updateSession(session);
            return new Response(true, "Session updated successfully", 
                Serializer.toJson(updatedSession));
                
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to update session", null);
        }
    }

    public Response deleteSession(Request request) {
        try {
            Session session = (Session) new Deserializer().extractData(request);
            if (session == null || session.getId() == null) {
                return new Response(false, "Invalid session ID", null);
            }
            
            Session sessionToDelete = sessionService.findEntity(session.getId());
            if (sessionToDelete == null) {
                return new Response(false, "Session not found", null);
            }
            
            sessionService.deleteEntity(sessionToDelete);
            return new Response(true, "Session deleted successfully", null);
            
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to delete session", null);
        }
    }
}
