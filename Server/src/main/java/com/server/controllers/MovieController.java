package com.server.controllers;

import com.server.exceptions.ResponseException;
import com.server.models.Movie;
import com.server.network.Request;
import com.server.network.Response;
import com.server.serializer.Deserializer;
import com.server.serializer.Serializer;
import com.server.services.GenreService;
import com.server.services.MovieService;

import java.util.List;

public class MovieController {
    private final MovieService movieService;
    private final GenreService genreService;

    public MovieController(MovieService movieService, GenreService genreService) {
        this.movieService = movieService;
        this.genreService = genreService;
    }

    public MovieController() {
        this.movieService = new MovieService();
        this.genreService = new GenreService();
    }

    public Response createMovie(Request request) {
        Object extractedData;
        try {
            extractedData = new Deserializer().extractData(request);
        } catch (IllegalArgumentException e) {
            return new Response(false, "Invalid movie data", null);
        }
        if (!(extractedData instanceof Movie movie)) {
            return new Response(false, "Invalid movie data", null);
        }
        try {
            return new Response(true, "Movie created successfully", Serializer.toJson(movieService.createMovie(movie)));
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllMovies() {
        try {
            List<Movie> movies = movieService.findAllEntities();
            String moviesJson = Serializer.toJson(movies);
            return new Response(true, "Movies retrieved successfully", moviesJson);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve movies", null);
        }
    }

    public Response getMovieByTitle(Request request) {
        Deserializer deserializer = new Deserializer();
        String title = (String) deserializer.extractData(request);
        try {
            Movie movieToFind = movieService.findEntityByTitle(title);
            if (movieToFind == null) {
                return new Response(false, "Movie not found", null);
            }
            String movieJson = Serializer.toJson(movieToFind);
            return new Response(true, "Movie retrieved successfully", movieJson);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve movie", null);
        }
    }

    public Response updateMovie(Request request) {
        Deserializer deserializer = new Deserializer();
        Movie updatedMovie = (Movie) deserializer.extractData(request);
        try {
            Movie updatedMovieEntity = movieService.updateMovie(updatedMovie);
            return new Response(true, "Movie updated successfully", Serializer.toJson(updatedMovieEntity));
        } catch (ResponseException e) {
            e.printStackTrace();
            return new Response(false, "An error occurred while updating the movie", null);
        }
    }

    public Response deleteMovie(Request request) {
        Movie movie = (Movie) new Deserializer().extractData(request);
        try {
            Movie existingMovie = movieService.findEntity(movie.getId());
            if (existingMovie == null) {
                return new Response(false, "Movie not found", null);
            }
            movieService.deleteEntity(existingMovie);
            return new Response(true, "Movie deleted successfully", null);
        } catch (ResponseException e) {
            e.printStackTrace();
            return new Response(false, "Failed to delete movie", null);
        }
    }
}