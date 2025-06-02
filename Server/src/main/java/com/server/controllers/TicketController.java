package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.Session;
import com.server.models.Ticket;
import com.server.models.User;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.NewDeserializer;
import com.server.serializer.Serializer;
import com.server.services.TicketService;

import java.util.List;

public class TicketController {
    private final TicketService ticketService;

    public TicketController() {
        this.ticketService = new TicketService();
    }

    public Response getAllTickets() {
        try {
            List<Ticket> tickets = ticketService.findAllEntities();
            return new Response(true, "Tickets retrieved successfully", 
                Serializer.toJson(tickets));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve tickets", null);
        }
    }

    public Response getTicketsBySession(Request request) {
        try {
            System.out.println("=== GET TICKETS BY SESSION REQUEST ===");
            System.out.println("Raw request data: " + request.getData());
            
            Object data = new NewDeserializer().extractData(request);
            if (!(data instanceof Session)) {
                String errorMsg = "Invalid session data format. Expected Session but got: " + 
                    (data != null ? data.getClass().getName() : "null");
                System.err.println(errorMsg);
                return new Response(false, errorMsg, null);
            }
            
            Session session = (Session) data;
            System.out.println("Getting tickets for session ID: " + session.getId());
            
            List<Ticket> tickets = ticketService.getTicketsBySession(session);
            System.out.println("Found " + tickets.size() + " tickets for session " + session.getId());
            
            return new Response(true, "Tickets retrieved successfully", 
                Serializer.toJson(tickets));
            
        } catch (ResponseException e) {
            System.err.println("ResponseException in getTicketsBySession: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Exception in getTicketsBySession: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, "Failed to retrieve tickets by session", null);
        } finally {
            System.out.println("=== END GET TICKETS BY SESSION REQUEST ===\n");
        }
    }

    public Response getTicketsByUser(Request request) {
        try {
            System.out.println("=== GET TICKETS BY USER REQUEST ===");
            System.out.println("Raw request data: " + request.getData());
            
            Object data = new NewDeserializer().extractData(request);
            if (!(data instanceof User)) {
                String errorMsg = "Invalid user data format. Expected User but got: " + 
                    (data != null ? data.getClass().getName() : "null");
                System.err.println(errorMsg);
                return new Response(false, errorMsg, null);
            }
            
            User user = (User) data;
            System.out.println("Getting tickets for user ID: " + user.getId());
            
            List<Ticket> tickets = ticketService.getTicketsByUser(user);
            System.out.println("Found " + tickets.size() + " tickets for user " + user.getId());
            
            return new Response(true, "Tickets retrieved successfully", 
                Serializer.toJson(tickets));
            
        } catch (ResponseException e) {
            System.err.println("ResponseException in getTicketsByUser: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Exception in getTicketsByUser: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, "Failed to retrieve tickets by user", null);
        } finally {
            System.out.println("=== END GET TICKETS BY USER REQUEST ===\n");
        }
    }

    public Response createTicket(Request request) {
        try {
            System.out.println("=== CREATE TICKET REQUEST ===");
            System.out.println("Raw request data: " + request.getData());
            
            // Extract and validate ticket data
            NewDeserializer deserializer = new NewDeserializer();
            Object data = deserializer.extractData(request);
            
            System.out.println("Deserialized data type: " + (data != null ? data.getClass().getName() : "null"));
            
            if (!(data instanceof Ticket)) {
                String errorMsg = "Invalid ticket data format. Expected Ticket but got: " + 
                    (data != null ? data.getClass().getName() : "null");
                System.err.println(errorMsg);
                return new Response(false, errorMsg, null);
            }
            
            Ticket ticket = (Ticket) data;
            System.out.println("Deserialized ticket:");
            System.out.println("  - ID: " + ticket.getId());
            System.out.println("  - Seat Number: " + ticket.getSeatNumber());
            
            // Check session
            if (ticket.getSession() == null) {
                System.err.println("Ticket session is null");
                return new Response(false, "Session is required", null);
            } else {
                System.out.println("  - Session ID: " + ticket.getSession().getId());
            }
            
            // Check user
            if (ticket.getUser() == null) {
                System.err.println("Ticket user is null");
                return new Response(false, "User is required", null);
            } else {
                System.out.println("  - User ID: " + ticket.getUser().getId());
            }
            
            System.out.println("Attempting to create ticket...");
            Ticket createdTicket = ticketService.createTicket(ticket);
            System.out.println("Successfully created ticket with ID: " + createdTicket.getId());
            
            return new Response(true, "Ticket created successfully", 
                Serializer.toJson(createdTicket));
            
        } catch (ResponseException e) {
            String errorMsg = "ResponseException in createTicket: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            String errorMsg = "Unexpected error in createTicket: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return new Response(false, "Failed to create ticket: " + e.getMessage(), null);
        } finally {
            System.out.println("=== END CREATE TICKET REQUEST ===\n");
        }
    }

    public Response updateTicket(Request request) {
        try {
            System.out.println("=== UPDATE TICKET REQUEST ===");
            System.out.println("Raw request data: " + request.getData());
            
            Object data = new NewDeserializer().extractData(request);
            if (!(data instanceof Ticket)) {
                String errorMsg = "Invalid ticket data format. Expected Ticket but got: " + 
                    (data != null ? data.getClass().getName() : "null");
                System.err.println(errorMsg);
                return new Response(false, "Invalid ticket data format", null);
            }
            
            Ticket ticket = (Ticket) data;
            System.out.println("Updating ticket ID: " + ticket.getId());
            
            if (ticket.getId() == null) {
                System.err.println("Ticket ID is required for update");
                return new Response(false, "Ticket ID is required for update", null);
            }
            
            // Verify the ticket exists
            Ticket existingTicket = ticketService.findEntity(ticket.getId());
            if (existingTicket == null) {
                System.err.println("Ticket not found with ID: " + ticket.getId());
                return new Response(false, "Ticket not found", null);
            }
            
            // Update the existing ticket with the new data
            if (ticket.getSession() != null) existingTicket.setSession(ticket.getSession());
            if (ticket.getUser() != null) existingTicket.setUser(ticket.getUser());
            if (ticket.getSeatNumber() != null) existingTicket.setSeatNumber(ticket.getSeatNumber());
            // Note: purchaseTime is not updated as it's set only once when creating the ticket
            
            // Save the updated ticket
            Ticket updatedTicket = ticketService.updateTicket(existingTicket);
            System.out.println("Successfully updated ticket ID: " + updatedTicket.getId());
            
            return new Response(true, "Ticket updated successfully", 
                Serializer.toJson(updatedTicket));
            
        } catch (ResponseException e) {
            System.err.println("ResponseException in updateTicket: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Exception in updateTicket: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, "Failed to update ticket: " + e.getMessage(), null);
        } finally {
            System.out.println("=== END UPDATE TICKET REQUEST ===\n");
        }
    }

    public Response deleteTicket(Request request) {
        try {
            System.out.println("=== DELETE TICKET REQUEST ===");
            System.out.println("Raw request data: " + request.getData());
            
            Object data = new NewDeserializer().extractData(request);
            Integer ticketId = null;
            
            if (data instanceof Ticket) {
                // If we got a Ticket object
                Ticket ticket = (Ticket) data;
                ticketId = ticket.getId();
            } else if (data instanceof Number) {
                // If we got a direct ID number
                ticketId = ((Number) data).intValue();
            } else if (data != null && data.toString().matches("\\d+")) {
                // If we got a string that can be parsed as a number
                ticketId = Integer.parseInt(data.toString());
            }
            
            if (ticketId == null) {
                String errorMsg = "Invalid ticket ID format. Expected number or Ticket object but got: " + 
                    (data != null ? data.getClass().getName() : "null");
                System.err.println(errorMsg);
                return new Response(false, "Invalid ticket ID format", null);
            }
            
            System.out.println("Deleting ticket ID: " + ticketId);
            
            Ticket ticketToDelete = ticketService.findEntity(ticketId);
            if (ticketToDelete == null) {
                System.err.println("Ticket not found with ID: " + ticketId);
                return new Response(false, "Ticket not found", null);
            }
            
            ticketService.deleteEntity(ticketToDelete);
            System.out.println("Successfully deleted ticket ID: " + ticketId);
            
            return new Response(true, "Ticket deleted successfully", null);
            
        } catch (ResponseException e) {
            System.err.println("ResponseException in deleteTicket: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Exception in deleteTicket: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, "Failed to delete ticket: " + e.getMessage(), null);
        } finally {
            System.out.println("=== END DELETE TICKET REQUEST ===\n");
        }
    }
}
