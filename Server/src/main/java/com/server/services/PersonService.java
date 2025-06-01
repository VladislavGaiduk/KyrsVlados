package com.server.services;

import com.server.exceptions.ResponseException;
import com.server.interfaces.Service;
import com.server.models.Person;
import com.server.repositories.PersonDAO;

import java.util.List;

public class PersonService implements Service<Person> {
    private final PersonDAO personDAO = new PersonDAO();

    @Override
    public Person findEntity(int id) {
        return personDAO.findById(id);
    }

    @Override
    public void saveEntity(Person person) {
        personDAO.save(person);
    }

    @Override
    public void deleteEntity(Person person) {
        if (person == null) {
            throw new ResponseException("Person not found.");
        }

        personDAO.delete(person);
    }

    @Override
    public void updateEntity(Person person) {
        personDAO.update(person);
    }

    @Override
    public List<Person> findAllEntities() {
        return personDAO.findAll();
    }
}

