package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ChatbotMessage;
import com.fix4home.fix4home.model.enums.ChatbotMessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ChatbotMessage entity
 * Provides data access methods for chatbot messages
 */
@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    
    /**
     * Find all messages by sessionId, ordered by pair sequence and created date
     */
    @Query("SELECT cm FROM ChatbotMessage cm WHERE " +
           "cm.sessionId = :sessionId " +
           "ORDER BY cm.pairSequence ASC, cm.createdAt ASC")
    List<ChatbotMessage> findBySessionIdOrderByPairSequence(@Param("sessionId") String sessionId);
    
    /**
     * Find all messages by user, ordered by created date (newest first)
     */
    @Query("SELECT cm FROM ChatbotMessage cm WHERE " +
           "cm.user.id = :userId " +
           "ORDER BY cm.createdAt DESC")
    Page<ChatbotMessage> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find messages by sessionId with pagination
     */
    @Query("SELECT cm FROM ChatbotMessage cm WHERE " +
           "cm.sessionId = :sessionId " +
           "ORDER BY cm.pairSequence ASC, cm.createdAt ASC")
    Page<ChatbotMessage> findBySessionIdOrderByPairSequence(@Param("sessionId") String sessionId, Pageable pageable);
    
    /**
     * Count messages in a session
     */
    long countBySessionId(String sessionId);
    
    /**
     * Find latest message in a session
     */
    @Query("SELECT cm FROM ChatbotMessage cm WHERE " +
           "cm.sessionId = :sessionId " +
           "ORDER BY cm.pairSequence DESC, cm.createdAt DESC")
    List<ChatbotMessage> findLatestBySessionId(@Param("sessionId") String sessionId, Pageable pageable);
    
    /**
     * Find messages by type in a session
     */
    List<ChatbotMessage> findBySessionIdAndMessageType(String sessionId, ChatbotMessageType messageType);
    
    /**
     * Count message pairs in a session (count distinct pair_sequence)
     */
    @Query("SELECT COUNT(DISTINCT cm.pairSequence) FROM ChatbotMessage cm WHERE cm.sessionId = :sessionId")
    long countMessagePairsBySessionId(@Param("sessionId") String sessionId);
}

