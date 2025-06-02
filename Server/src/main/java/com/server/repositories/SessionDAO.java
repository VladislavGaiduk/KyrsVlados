package com.server.repositories;

import com.server.config.SessionConfig;
import com.server.interfaces.DAO;
import com.server.models.Session;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;


public class SessionDAO implements DAO<Session> {
    private final SessionFactory sessionFactory;

    public SessionDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Session obj) {
        executeTransaction(sessionFactory, org.hibernate.Session::persist, obj);
    }

    @Override
    public void update(Session obj) {
        executeTransaction(sessionFactory, org.hibernate.Session::merge, obj);
    }

    @Override
    public void delete(Session obj) {
        executeTransaction(sessionFactory, (org.hibernate.Session session, com.server.models.Session session1) ->
                session.remove(session.contains(session1) ? session1 : session.merge(session1)), obj);
    }


    @Override
    public Session findById(int id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.get(Session.class, id);
        }
    }


    @Override
    public List<Session> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Session> criteriaQuery = criteriaBuilder.createQuery(Session.class);
            Root<Session> root = criteriaQuery.from(Session.class);
            criteriaQuery.select(root);
            Query<Session> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public List<Session> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Session> cq = cb.createQuery(Session.class);
            Root<Session> root = cq.from(Session.class);
            
            cq.select(root)
              .where(cb.and(
                  cb.greaterThanOrEqualTo(root.get("startTime"), startDate),
                  cb.lessThanOrEqualTo(root.get("startTime"), endDate)
              ))
              .orderBy(cb.asc(root.get("startTime")));
            
            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find sessions by date range", e);
        }
    }
    
    public List<Session> findByHallAndDateRange(Integer hallId, LocalDateTime startDate, LocalDateTime endDate) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Session> cq = cb.createQuery(Session.class);
            Root<Session> root = cq.from(Session.class);
            
            // Find sessions where:
            // 1. The session is in the specified hall
            // 2. The session time range overlaps with the specified range
            //    (start1 < end2 AND end1 > start2)
            cq.select(root)
              .where(cb.and(
                  cb.equal(root.get("hall").get("id"), hallId),
                  cb.lessThan(root.get("startTime"), endDate),
                  cb.greaterThan(root.get("endTime"), startDate)
              ))
              .orderBy(cb.asc(root.get("startTime")));
            
            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find sessions by hall and date range", e);
        }
    }
}
