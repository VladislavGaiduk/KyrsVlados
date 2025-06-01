package com.server.services;

import com.server.exceptions.ResponseException;
import com.server.interfaces.Service;
import com.server.models.Movie;
import com.server.repositories.MovieDAO;

import java.util.List;

public class MovieService implements Service<Movie> {
    private final MovieDAO movieDAO = new MovieDAO();

    @Override
    public Movie findEntity(int id) {
        return movieDAO.findById(id);
    }

    @Override
    public void saveEntity(Movie movie) {
        movieDAO.save(movie);
    }

    @Override
    public void deleteEntity(Movie movie) {
        if (movie == null) {
            throw new ResponseException("Movie not found.");
        }
        movieDAO.delete(movie);
    }

    @Override
    public void updateEntity(Movie movie) {
        movieDAO.update(movie);
    }

    @Override
    public List<Movie> findAllEntities() {
        return movieDAO.findAll();
    }

    public Movie findEntityByTitle(String title) {
        return movieDAO.findByTitle(title);
    }

    public Movie createMovie(Movie movie) throws ResponseException {
        saveEntity(movie);
        return movie;
    }

    public Movie updateMovie(Movie updatedMovie) throws ResponseException {
        Movie existingMovie = findEntity(updatedMovie.getId());
        if (existingMovie == null) {
            throw new ResponseException("Movie not found");
        }
        updateEntity(updatedMovie);
        return updatedMovie;
    }
}