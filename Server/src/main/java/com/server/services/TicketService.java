package com.server.services;

import com.server.exceptions.ResponseException;
import com.server.interfaces.Service;
import com.server.models.Session;
import com.server.models.Ticket;
import com.server.models.User;
import com.server.repositories.TicketDAO;

import java.util.List;

public class TicketService implements Service<Ticket> {
    private final TicketDAO ticketDAO;
    private final SessionService sessionService;
    private final UserService userService;

    public TicketService() {
        this.ticketDAO = new TicketDAO();
        this.sessionService = new SessionService();
        this.userService = new UserService();
    }

    @Override
    public Ticket findEntity(int id) {
        return ticketDAO.findById(id);
    }

    @Override
    public void saveEntity(Ticket ticket) {
        ticketDAO.save(ticket);
    }

    @Override
    public void deleteEntity(Ticket ticket) {
        if (ticket == null) {
            throw new ResponseException("Ticket not found");
        }
        ticketDAO.delete(ticket);
    }

    @Override
    public void updateEntity(Ticket ticket) {
        ticketDAO.update(ticket);
    }

    @Override
    public List<Ticket> findAllEntities() {
        return ticketDAO.findAll();
    }

    public Ticket createTicket(Ticket ticket) throws ResponseException {
        validateTicket(ticket);
        ticketDAO.save(ticket);
        return ticket;
    }

    public Ticket updateTicket(Ticket updatedTicket) throws ResponseException {
        Ticket existingTicket = findEntity(updatedTicket.getId());
        if (existingTicket == null) {
            throw new ResponseException("Ticket not found");
        }
        
        validateTicket(updatedTicket);
        ticketDAO.update(updatedTicket);
        return updatedTicket;
    }

    public List<Ticket> getTicketsBySession(Session session) {
        return ticketDAO.findBySession(session);
    }

    public List<Ticket> getTicketsByUser(User user) {
        return ticketDAO.findByUser(user);
    }

    private void validateTicket(Ticket ticket) throws ResponseException {
        if (ticket == null) {
            throw new ResponseException("Ticket cannot be null");
        }

        if (ticket.getSession() == null || ticket.getSession().getId() == null) {
            throw new ResponseException("Session is required");
        }

        if (ticket.getUser() == null || ticket.getUser().getId() == null) {
            throw new ResponseException("User is required");
        }

        if (ticket.getSeatNumber() == null || ticket.getSeatNumber() <= 0) {
            throw new ResponseException("Invalid seat number");
        }

        // Verify session exists
        Session session = sessionService.findEntity(ticket.getSession().getId());
        if (session == null) {
            throw new ResponseException("Session not found");
        }

        // Verify user exists
        User user = userService.findEntity(ticket.getUser().getId());
        if (user == null) {
            throw new ResponseException("User not found");
        }

        // Check if seat is already taken for this session
        if (ticketDAO.isSeatTaken(session, ticket.getSeatNumber())) {
            // For update case, check if it's the same ticket
            if (ticket.getId() == null || !isSameTicket(ticket.getId(), session, ticket.getSeatNumber())) {
                throw new ResponseException("Seat " + ticket.getSeatNumber() + " is already taken for this session");
            }
        }
    }

    private boolean isSameTicket(Integer ticketId, Session session, int seatNumber) {
        List<Ticket> tickets = ticketDAO.findBySession(session);
        return tickets.stream()
                .anyMatch(t -> t.getId().equals(ticketId) && t.getSeatNumber() == seatNumber);
    }
}
