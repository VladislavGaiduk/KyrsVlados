package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.Genre;
import com.server.models.Movie;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;
import com.server.services.GenreService;

import java.util.List;

public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    public GenreController() {
        this.genreService = new GenreService();
    }

    public Response createGenre(Request request) {
        Object extractedData;
        try {
            extractedData = new Deserializer().extractData(request);
        } catch (IllegalArgumentException e) {
            return new Response(false, "Invalid genre data", null);
        }
        if (!(extractedData instanceof Genre genre)) {
            return new Response(false, "Invalid genre data", null);
        }
        try {
            return new Response(true, "Genre created successfully", Serializer.toJson(genreService.createGenre(genre)));
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllGenres() {
        try {
            List<Genre> genres = genreService.findAllEntities();
            String genresJson = Serializer.toJson(genres);
            return new Response(true, "Genres retrieved successfully", genresJson);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve genres", null);
        }
    }

    public Response updateGenre(Request request) {
        Deserializer deserializer = new Deserializer();
        Genre updatedGenre = ( Genre) deserializer.extractData(request);
        try {
            Genre updatedMovieEntity = genreService.updateGenre(updatedGenre);
            return new Response(true, "Movie updated successfully", Serializer.toJson(updatedMovieEntity));
        } catch (ResponseException e) {
            e.printStackTrace();
            return new Response(false, "An error occurred while updating the movie", null);
        }
    }

    public Response deleteGenre(Request request) {
           Genre genre = (Genre) new Deserializer().extractData(request);
            try {
               Genre existingMovie = genreService.findEntity(genre.getId());
                if (existingMovie == null) {
                    return new Response(false, "Movie not found", null);
                }
                genreService.deleteEntity(existingMovie);
                return new Response(true, "Movie deleted successfully", null);
            } catch (ResponseException e) {
                e.printStackTrace();
                return new Response(false, "Failed to delete movie", null);
            }
    }
}