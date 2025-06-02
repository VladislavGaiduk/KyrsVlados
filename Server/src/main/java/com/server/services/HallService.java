package com.server.services;

import com.server.exceptions.ResponseException;
import com.server.interfaces.Service;
import com.server.models.Hall;
import com.server.repositories.HallDAO;

import java.util.List;

public class HallService implements Service<Hall> {
    private final HallDAO hallDAO = new HallDAO();

    @Override
    public Hall findEntity(int id) {
        return hallDAO.findById(id);
    }

    @Override
    public void saveEntity(Hall hall) {
        hallDAO.save(hall);
    }

    @Override
    public void deleteEntity(Hall hall) {
        if (hall == null) {
            throw new ResponseException("Hall not found.");
        }
        hallDAO.delete(hall);
    }

    @Override
    public void updateEntity(Hall hall) {
        hallDAO.update(hall);
    }

    @Override
    public List<Hall> findAllEntities() {
        return hallDAO.findAll();
    }

    public Hall findEntityByName(String name) {
        return hallDAO.findByName(name);
    }

    public Hall createHall(Hall hall) throws ResponseException {
        // Check if hall with the same name already exists
        Hall existingHall = findEntityByName(hall.getName());
        if (existingHall != null) {
            throw new ResponseException("Hall with this name already exists");
        }
        
        // Validate capacity
        if (hall.getCapacity() <= 0) {
            throw new ResponseException("Hall capacity must be a positive number");
        }
        
        saveEntity(hall);
        return hall;
    }

    public Hall updateHall(Hall updatedHall) throws ResponseException {
        Hall existingHall = findEntity(updatedHall.getId());
        if (existingHall == null) {
            throw new ResponseException("Hall not found");
        }
        
        // Check if another hall with the same name exists
        Hall hallWithSameName = findEntityByName(updatedHall.getName());
        if (hallWithSameName != null && !hallWithSameName.getId().equals(updatedHall.getId())) {
            throw new ResponseException("Another hall with this name already exists");
        }
        
        // Validate capacity
        if (updatedHall.getCapacity() <= 0) {
            throw new ResponseException("Hall capacity must be a positive number");
        }
        
        updateEntity(updatedHall);
        return updatedHall;
    }
}
