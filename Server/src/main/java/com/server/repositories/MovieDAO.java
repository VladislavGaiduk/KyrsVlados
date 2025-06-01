package com.server.repositories;

import com.server.config.SessionConfig;
import com.server.interfaces.DAO;
import com.server.models.Movie;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class MovieDAO implements DAO<Movie> {
    private final SessionFactory sessionFactory;

    public MovieDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Movie obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(Movie obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(Movie obj) {
        executeTransaction(sessionFactory, (session, movie) ->
                session.remove(session.contains(movie) ? movie : session.merge(movie)), obj);
    }

    @Override
    public Movie findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Movie.class, id);
        }
    }

    @Override
    public List<Movie> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Movie> criteriaQuery = criteriaBuilder.createQuery(Movie.class);
            Root<Movie> root = criteriaQuery.from(Movie.class);
            criteriaQuery.select(root);
            Query<Movie> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public Movie findByTitle(String title) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Movie> criteriaQuery = criteriaBuilder.createQuery(Movie.class);
            Root<Movie> root = criteriaQuery.from(Movie.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("title"), title));
            Query<Movie> query = session.createQuery(criteriaQuery);
            return query.uniqueResult();
        }
    }
}