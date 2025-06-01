package com.server.config;

import com.server.models.*;
import com.server.models.Person;
import com.server.models.Role;
import com.server.models.User;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Getter
public class SessionConfig {
    private static SessionConfig sessionConfig;

    private final SessionFactory sessionFactory;

    private SessionConfig() {
        sessionFactory = new Configuration().
                addAnnotatedClass(User.class).
                addAnnotatedClass(Person.class).
                addAnnotatedClass(Role.class).
                addAnnotatedClass(Genre.class).
                addAnnotatedClass(Movie.class).
                buildSessionFactory();
    }

    synchronized public static SessionConfig getInstance() {
        if (sessionConfig == null) {
            sessionConfig = new SessionConfig();
        }

        return sessionConfig;
    }
}
