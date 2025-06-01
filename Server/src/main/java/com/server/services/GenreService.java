package com.server.services;

import com.server.exceptions.ResponseException;
import com.server.interfaces.Service;
import com.server.models.Genre;
import com.server.models.Movie;
import com.server.repositories.GenreDAO;

import java.util.List;

public class GenreService  {
    private final GenreDAO genreDAO = new GenreDAO();

    public Genre findEntity(int id) {
        return genreDAO.findById(id);
    }


    public Genre createGenre(Genre genre) {
        genreDAO.save(genre);
        return genre;
    }


    public void deleteEntity(Genre genre) {
        genreDAO.delete(genre);
    }


    public void updateEntity(Genre genre) {
        genreDAO.update(genre);
    }


    public List<Genre> findAllEntities() {
        return genreDAO.findAll();
    }

    public Genre findEntityByName(String name) {
        return genreDAO.findByName(name);
    }

    public Genre updateGenre(Genre updatedGenre) {
        Genre existingGenre = findEntity(updatedGenre.getId());
        if (existingGenre == null) {
            throw new ResponseException("Movie not found");
        }
        updateEntity(updatedGenre);
        return updatedGenre;
    }
}