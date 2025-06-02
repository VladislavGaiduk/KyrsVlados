package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.Hall;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;
import com.server.services.HallService;

public class HallController {
    private final HallService hallService;

    public HallController() {
        this.hallService = new HallService();
    }

    public Response getAllHalls() {
        try {
            return new Response(true, "Halls retrieved successfully", 
                Serializer.toJson(hallService.findAllEntities()));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve halls", null);
        }
    }

    public Response createHall(Request request) {
        try {
            Hall hall = (Hall) new Deserializer().extractData(request);
            if (hall == null) {
                return new Response(false, "Invalid hall data", null);
            }
            
            Hall createdHall = hallService.createHall(hall);
            return new Response(true, "Hall created successfully", 
                Serializer.toJson(createdHall));
                
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to create hall", null);
        }
    }

    public Response updateHall(Request request) {
        try {
            Hall hall = (Hall) new Deserializer().extractData(request);
            if (hall == null) {
                return new Response(false, "Invalid hall data", null);
            }
            
            Hall updatedHall = hallService.updateHall(hall);
            return new Response(true, "Hall updated successfully", 
                Serializer.toJson(updatedHall));
                
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to update hall", null);
        }
    }

    public Response deleteHall(Request request) {
        try {
            Hall hall = (Hall) new Deserializer().extractData(request);
            if (hall == null || hall.getId() == null) {
                return new Response(false, "Invalid hall ID", null);
            }
            
            Hall hallToDelete = hallService.findEntity(hall.getId());
            if (hallToDelete == null) {
                return new Response(false, "Hall not found", null);
            }
            
            hallService.deleteEntity(hallToDelete);
            return new Response(true, "Hall deleted successfully", null);
            
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to delete hall", null);
        }
    }
}
