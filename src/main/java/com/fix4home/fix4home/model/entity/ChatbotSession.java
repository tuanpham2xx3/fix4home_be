package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ChatbotSession entity represents a conversation session with the n8n chatbot
 * Each session has a unique 8-digit sessionId and tracks message pairs
 */
@Entity
@Table(name = "chatbot_sessions", indexes = {
    @Index(name = "idx_chatbot_sessions_session_id", columnList = "session_id"),
    @Index(name = "idx_chatbot_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_chatbot_sessions_user_active", columnList = "user_id, is_active, created_at"),
    @Index(name = "idx_chatbot_sessions_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 8-digit session ID for n8n webhook
     */
    @Column(name = "session_id", nullable = false, unique = true, length = 8)
    private String sessionId;
    
    /**
     * User who owns this session
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Number of complete message pairs (user message + bot response)
     */
    @Column(name = "message_count", nullable = false)
    @Builder.Default
    private Integer messageCount = 0;
    
    /**
     * Whether this session is currently active
     * Active sessions are current, inactive sessions are completed (reached 5 pairs)
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Increment message count after a complete pair
     */
    public void incrementMessageCount() {
        this.messageCount++;
    }
    
    /**
     * Check if session has reached the maximum message pairs
     */
    public boolean hasReachedMaxPairs(int maxPairs) {
        return this.messageCount >= maxPairs;
    }
    
    /**
     * Mark session as inactive (completed)
     */
    public void deactivate() {
        this.isActive = false;
    }
}

