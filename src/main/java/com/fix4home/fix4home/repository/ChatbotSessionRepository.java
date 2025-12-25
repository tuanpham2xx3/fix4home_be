package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ChatbotSession;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ChatbotSession entity
 * Provides data access methods for chatbot sessions
 */
@Repository
public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {
    
    /**
     * Find session by sessionId (8-digit string)
     */
    Optional<ChatbotSession> findBySessionId(String sessionId);
    
    /**
     * Find active session for user (most recent active session)
     */
    @Query("SELECT cs FROM ChatbotSession cs WHERE " +
           "cs.user = :user AND " +
           "cs.isActive = true " +
           "ORDER BY cs.createdAt DESC")
    Optional<ChatbotSession> findActiveSessionByUser(@Param("user") User user);
    
    /**
     * Find active session for user ID
     */
    @Query("SELECT cs FROM ChatbotSession cs WHERE " +
           "cs.user.id = :userId AND " +
           "cs.isActive = true " +
           "ORDER BY cs.createdAt DESC")
    Optional<ChatbotSession> findActiveSessionByUserId(@Param("userId") Long userId);
    
    /**
     * Check if sessionId exists
     */
    boolean existsBySessionId(String sessionId);
    
    /**
     * Find all sessions for user (ordered by created date, newest first)
     */
    @Query("SELECT cs FROM ChatbotSession cs WHERE " +
           "cs.user.id = :userId " +
           "ORDER BY cs.createdAt DESC")
    java.util.List<ChatbotSession> findAllByUserId(@Param("userId") Long userId);
}

