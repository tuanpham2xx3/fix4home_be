package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ChatbotMessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ChatbotMessage entity represents individual messages in chatbot conversations
 * Stores both user messages and bot responses with pair sequence tracking
 */
@Entity
@Table(name = "chatbot_messages", indexes = {
    @Index(name = "idx_chatbot_messages_session_id", columnList = "session_id"),
    @Index(name = "idx_chatbot_messages_user_id", columnList = "user_id"),
    @Index(name = "idx_chatbot_messages_session_sequence", columnList = "session_id, pair_sequence, created_at"),
    @Index(name = "idx_chatbot_messages_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_chatbot_messages_type", columnList = "message_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Session ID this message belongs to (8-digit string)
     */
    @Column(name = "session_id", nullable = false, length = 8)
    private String sessionId;
    
    /**
     * User associated with this message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * The actual message content
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    /**
     * Type of message: USER or BOT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private ChatbotMessageType messageType;
    
    /**
     * Sequence number of the message pair (1st pair=1, 2nd pair=2, etc.)
     */
    @Column(name = "pair_sequence", nullable = false)
    private Integer pairSequence;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Check if this is a user message
     */
    public boolean isUserMessage() {
        return this.messageType == ChatbotMessageType.USER;
    }
    
    /**
     * Check if this is a bot response
     */
    public boolean isBotMessage() {
        return this.messageType == ChatbotMessageType.BOT;
    }
}

