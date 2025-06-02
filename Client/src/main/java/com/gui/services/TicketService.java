package com.gui.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gui.utils.LocalDateTimeAdapter;
import com.server.enums.Operation;
import com.server.models.Session;
import com.server.models.Ticket;
import com.server.models.User;
import com.server.network.Request;
import com.server.network.Response;
import com.server.network.ServerClient;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketService {
    private final Gson gson;

    public TicketService() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    }

    public Response getAllTickets() {
        try {
            Request request = new Request(Operation.GET_ALL_TICKETS);
            return ServerClient.getInstance().sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при получении списка билетов: " + e.getMessage(), null);
        }
    }

    public Response getTicketsBySession(Integer sessionId) {
        try {
            if (sessionId == null) {
                return new Response(false, "ИД сеанса обязателен", null);
            }
            Request request = new Request(Operation.GET_TICKETS_BY_SESSION, String.valueOf(sessionId));
            return ServerClient.getInstance().sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при получении билетов по сеансу: " + e.getMessage(), null);
        }
    }

    public Response getTicketsByUser(Integer userId) {
        try {
            if (userId == null) {
                return new Response(false, "ИД пользователя обязателен", null);
            }
            Request request = new Request(Operation.GET_TICKETS_BY_USER, String.valueOf(userId));
            return ServerClient.getInstance().sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при получении билетов пользователя: " + e.getMessage(), null);
        }
    }

    public Response createTicket(Ticket ticket) {
        try {
            if (ticket == null) {
                return new Response(false, "Данные билета обязательны", null);
            }
            if (ticket.getSession() == null || ticket.getSession().getId() == null) {
                return new Response(false, "Сеанс обязателен", null);
            }
            if (ticket.getUser() == null || ticket.getUser().getId() == null) {
                return new Response(false, "Пользователь обязателен", null);
            }
            if (ticket.getSeatNumber() == null || ticket.getSeatNumber() <= 0) {
                return new Response(false, "Некорректный номер места", null);
            }

            String ticketJson = gson.toJson(ticket);
            Request request = new Request(Operation.CREATE_TICKET, ticketJson);
            return ServerClient.getInstance().sendRequest(request);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при создании билета: " + e.getMessage(), null);
        }
    }

    public Response updateTicket(Ticket ticket) {
        try {
            if (ticket == null || ticket.getId() == null) {
                return new Response(false, "ИД билета обязателен", null);
            }
            if (ticket.getSession() == null || ticket.getSession().getId() == null) {
                return new Response(false, "Сеанс обязателен", null);
            }
            if (ticket.getUser() == null || ticket.getUser().getId() == null) {
                return new Response(false, "Пользователь обязателен", null);
            }
            if (ticket.getSeatNumber() == null || ticket.getSeatNumber() <= 0) {
                return new Response(false, "Некорректный номер места", null);
            }

            String ticketJson = gson.toJson(ticket);
            Request request = new Request(Operation.UPDATE_TICKET, ticketJson);
            return ServerClient.getInstance().sendRequest(request);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при обновлении билета: " + e.getMessage(), null);
        }
    }

    public Response deleteTicket(Integer ticketId) {
        try {
            if (ticketId == null) {
                return new Response(false, "ИД билета обязателен", null);
            }
            Request request = new Request(Operation.DELETE_TICKET, String.valueOf(ticketId));
            return ServerClient.getInstance().sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка при удалении билета: " + e.getMessage(), null);
        }
    }
}
