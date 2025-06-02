package com.server.services;

import com.server.exceptions.ResponseException;
import com.server.interfaces.Service;
import com.server.models.Session;
import com.server.repositories.SessionDAO;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class SessionService implements Service<Session> {
    private final SessionDAO sessionDAO;
    private final MovieService movieService;
    private final HallService hallService;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
        this.movieService = new MovieService();
        this.hallService = new HallService();
    }

    @Override
    public Session findEntity(int id) {
        return sessionDAO.findById(id);
    }

    @Override
    public void saveEntity(Session session) {
        sessionDAO.save(session);
    }

    @Override
    public void deleteEntity(Session session) {
        if (session == null) {
            throw new ResponseException("Session not found.");
        }
        sessionDAO.delete(session);
    }

    @Override
    public void updateEntity(Session session) {
        sessionDAO.update(session);
    }

    @Override
    public List<Session> findAllEntities() {
        return sessionDAO.findAll();
    }

    public List<Session> findSessionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new ResponseException("Invalid date range");
        }
        return sessionDAO.findByDateRange(startDate, endDate);
    }

    public Session createSession(Session session) throws ResponseException {
        Transaction transaction = null;
        try {
            // First validate the session
            validateSession(session);
            
            // Check for overlapping sessions in the same hall
            checkForOverlappingSessions(session);
            
            // Save the session
            saveEntity(session);
            return session;
        } catch (ResponseException e) {
            throw e; // Re-throw validation/overlap errors
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new ResponseException("Failed to create session: " + e.getMessage());
        }
    }

    public Session updateSession(Session updatedSession) throws ResponseException {
        Session existingSession = findEntity(updatedSession.getId());
        if (existingSession == null) {
            throw new ResponseException("Session not found");
        }
        
        validateSession(updatedSession);
        checkForOverlappingSessions(updatedSession, updatedSession.getId());
        
        updateEntity(updatedSession);
        return updatedSession;
    }

    private void validateSession(Session session) {
        if (session == null) {
            throw new ResponseException("Session cannot be null");
        }
        
        if (session.getMovie() == null || session.getMovie().getId() == null) {
            throw new ResponseException("Movie is required");
        }
        
        if (session.getHall() == null || session.getHall().getId() == null) {
            throw new ResponseException("Hall is required");
        }
        
        if (session.getStartTime() == null || session.getEndTime() == null) {
            throw new ResponseException("Start time and end time are required");
        }
        
        // Check if session is in the future
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseException("Cannot create session in the past");
        }
        
        // Check if end time is after start time
        if (!session.getEndTime().isAfter(session.getStartTime())) {
            throw new ResponseException("End time must be after start time");
        }
        
        // Check if duration is reasonable (e.g., at least 1 minute, less than 12 hours)
        long durationMinutes = java.time.Duration.between(
            session.getStartTime(), session.getEndTime()
        ).toMinutes();
        
        if (durationMinutes < 1) {
            throw new ResponseException("Session duration must be at least 1 minute");
        }
        
        if (durationMinutes > 12 * 60) {
            throw new ResponseException("Session duration cannot exceed 12 hours");
        }
        
        if (session.getPrice() == 0 ) {
            throw new ResponseException("Price must be greater than 0");
        }
        
        try {
            // Verify movie exists
            if (movieService.findEntity(session.getMovie().getId()) == null) {
                throw new ResponseException("Movie not found");
            }
            
            // Verify hall exists
            if (hallService.findEntity(session.getHall().getId()) == null) {
                throw new ResponseException("Hall not found");
            }
        } catch (Exception e) {
            throw new ResponseException("Error validating session: " + e.getMessage());
        }
    }

    private void checkForOverlappingSessions(Session session) {
        checkForOverlappingSessions(session, null);
    }

    private void checkForOverlappingSessions(Session session, Integer excludeSessionId) {
        // Only check sessions in the same hall and within the same day
        LocalDateTime startOfDay = session.getStartTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<Session> sessionsInSameHall = sessionDAO.findByHallAndDateRange(
            session.getHall().getId(), 
            startOfDay, 
            endOfDay
        );
        
        for (Session existing : sessionsInSameHall) {
            if (excludeSessionId != null && existing.getId().equals(excludeSessionId)) {
                continue;
            }
            
            if (sessionsOverlap(existing, session)) {
                throw new ResponseException(String.format(
                    "Session overlaps with an existing session in the same hall (ID: %d, Time: %s - %s)",
                    existing.getId(),
                    existing.getStartTime(),
                    existing.getEndTime()
                ));
            }
        }
    }

    private boolean sessionsOverlap(Session s1, Session s2) {
        // Check if the sessions overlap (start1 < end2 && end1 > start2)
        return s1.getStartTime().isBefore(s2.getEndTime()) && 
               s1.getEndTime().isAfter(s2.getStartTime());
    }
}
