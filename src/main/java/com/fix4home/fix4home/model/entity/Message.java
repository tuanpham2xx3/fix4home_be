package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Message entity represents individual messages within a conversation
 * Supports different message types including text, images, location, etc.
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Conversation reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    
    // Message sender
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    // Message content
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    
    // For file attachments, images, etc.
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
    
    // JSON metadata for additional information (location coordinates, pricing, etc.)
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    // Message status
    @Builder.Default
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
    
    // Helper methods
    public boolean isFromUser(User user) {
        return this.sender.getId().equals(user.getId());
    }
    
    public boolean isSystemMessage() {
        return this.messageType == MessageType.SYSTEM;
    }
    
    public boolean hasAttachment() {
        return this.attachmentUrl != null && !this.attachmentUrl.trim().isEmpty();
    }
} 