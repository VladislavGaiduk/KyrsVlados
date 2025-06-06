package com.gui.enums;

public enum StagePath {
    LOGIN("views/Login.fxml"),
    REGISTRATION("views/Registration.fxml"),
    ADMIN_MOVIES_MANAGMENT("views/Admin_movies.fxml"),
    MAIN_MENU("views/Start_menu.fxml"),
    ADMIN_USERS_MANAGEMENT("views/Admin_users.fxml"),
    ADMIN_GENRES_MANAGEMENT("views/Admin_genres.fxml"),
    ADMIN_ROLES_MANAGEMENT("views/Admin_roles.fxml"),
    ADMIN_HALLS_MANAGEMENT("views/Admin_halls.fxml"),
    ADMIN_SESSIONS_MANAGEMENT("views/Admin_sessions.fxml"),
    ADMIN_TICKETS_MANAGEMENT("views/Admin_tickets.fxml"),
    ADMIN_PROFILE("views/Admin_profile.fxml");

    private final String pathToFxml;

    StagePath(String pathToFxml) {
        this.pathToFxml = pathToFxml;
    }

    public String getPathToFxml() {
        return pathToFxml;
    }
}

