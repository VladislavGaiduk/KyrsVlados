package com.server.repositories;

import com.server.config.SessionConfig;
import com.server.interfaces.DAO;
import com.server.models.Genre;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class GenreDAO implements DAO<Genre> {
    private final SessionFactory sessionFactory;

    public GenreDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Genre obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(Genre obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(Genre obj) {
        executeTransaction(sessionFactory, (session, genre) ->
                session.remove(session.contains(genre) ? genre : session.merge(genre)), obj);
    }

    @Override
    public Genre findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Genre.class, id);
        }
    }

    @Override
    public List<Genre> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Genre> criteriaQuery = criteriaBuilder.createQuery(Genre.class);
            Root<Genre> root = criteriaQuery.from(Genre.class);
            criteriaQuery.select(root);
            Query<Genre> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public Genre findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Genre> criteriaQuery = criteriaBuilder.createQuery(Genre.class);
            Root<Genre> root = criteriaQuery.from(Genre.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), name));
            Query<Genre> query = session.createQuery(criteriaQuery);
            return query.uniqueResultOptional().orElse(null);
        }
    }
}