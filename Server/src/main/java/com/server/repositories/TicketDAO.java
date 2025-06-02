package com.server.repositories;

import com.server.config.SessionConfig;
import com.server.interfaces.DAO;
import com.server.models.Session;
import com.server.models.Ticket;
import com.server.models.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class TicketDAO implements DAO<Ticket> {
    private final SessionFactory sessionFactory;

    public TicketDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Ticket obj) {
        executeTransaction(sessionFactory, org.hibernate.Session::persist, obj);
    }

    @Override
    public void update(Ticket obj) {
        executeTransaction(sessionFactory, org.hibernate.Session::merge, obj);
    }

    @Override
    public void delete(Ticket obj) {
        executeTransaction(sessionFactory, (session, ticket) ->
            session.remove(session.contains(ticket) ? ticket : session.merge(ticket)), obj);
    }

    @Override
    public Ticket findById(int id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.get(Ticket.class, id);
        }
    }

    @Override
    public List<Ticket> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> criteriaQuery = criteriaBuilder.createQuery(Ticket.class);
            Root<Ticket> root = criteriaQuery.from(Ticket.class);
            criteriaQuery.select(root);
            Query<Ticket> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public List<Ticket> findBySession(Session sessionObj) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> criteriaQuery = criteriaBuilder.createQuery(Ticket.class);
            Root<Ticket> root = criteriaQuery.from(Ticket.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("session"), sessionObj));
            Query<Ticket> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public List<Ticket> findByUser(User user) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> criteriaQuery = criteriaBuilder.createQuery(Ticket.class);
            Root<Ticket> root = criteriaQuery.from(Ticket.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("user"), user));
            Query<Ticket> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public boolean isSeatTaken(Session sessionObj, int seatNumber) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<Ticket> root = criteriaQuery.from(Ticket.class);
            
            criteriaQuery.select(criteriaBuilder.count(root));
            criteriaQuery.where(
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("session"), sessionObj),
                    criteriaBuilder.equal(root.get("seatNumber"), seatNumber)
                )
            );
            
            Long count = session.createQuery(criteriaQuery).uniqueResult();
            return count != null && count > 0;
        }
    }
}
