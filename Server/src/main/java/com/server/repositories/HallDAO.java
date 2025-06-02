package com.server.repositories;

import com.server.config.SessionConfig;
import com.server.interfaces.DAO;
import com.server.models.Hall;
import com.server.models.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class HallDAO implements DAO<Hall> {
    private final SessionFactory sessionFactory;

    public HallDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Hall obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(Hall obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(Hall obj) {
        executeTransaction(sessionFactory, (session, hall) ->
                session.remove(session.contains(hall) ? hall : session.merge(hall)), obj);
    }

    @Override
    public Hall findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Hall.class, id);
        }
    }

    @Override
    public List<Hall> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Hall> criteriaQuery = criteriaBuilder.createQuery(Hall.class);
            Root<Hall> root = criteriaQuery.from(Hall.class);
            criteriaQuery.select(root);
            Query<Hall> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public Hall findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Hall> criteriaQuery = criteriaBuilder.createQuery(Hall.class);
            Root<Hall> root = criteriaQuery.from(Hall.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), name));
            Query<Hall> query = session.createQuery(criteriaQuery);
            return query.uniqueResult();
        }
    }
}
