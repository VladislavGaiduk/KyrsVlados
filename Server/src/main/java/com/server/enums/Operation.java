package com.server.enums;

public enum Operation {
    // User operations
    LOGIN,
    REGISTER,
    DELETE_USER,
    READ_USER,
    UPDATE_USER,
    GET_ALL_USERS,
    
    // Role operations
    GET_ALL_ROLES,
    CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE,
    
    // Movie operations
    CREATE_MOVIE,
    UPDATE_MOVIE,
    DELETE_MOVIE,
    GET_ALL_MOVIES,
    
    // Genre operations
    GET_ALL_GENRES,
    CREATE_GENRE,
    UPDATE_GENRE,
    DELETE_GENRE,
    
    // Hall operations
    GET_ALL_HALLS,
    CREATE_HALL,
    UPDATE_HALL,
    DELETE_HALL,
    
    // Session operations
    GET_ALL_SESSIONS,
    GET_SESSIONS_BY_DATE_RANGE,
    CREATE_SESSION,
    UPDATE_SESSION,
    DELETE_SESSION,
    
    // Ticket operations
    GET_ALL_TICKETS,
    GET_TICKETS_BY_SESSION,
    GET_TICKETS_BY_USER,
    CREATE_TICKET,
    UPDATE_TICKET,
    DELETE_TICKET,
    
    // System operations
    DISCONNECT
}
