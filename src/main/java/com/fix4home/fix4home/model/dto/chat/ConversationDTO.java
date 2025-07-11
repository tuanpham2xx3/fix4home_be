package com.fix4home.fix4home.model.dto.chat;

import com.fix4home.fix4home.model.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Conversation entity
 * Contains conversation information with participant details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {
    
    private Long id;
    private ConversationStatus status;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business context (one of these will be set)
    private Long serviceRequestId;
    private Long servicePostId;
    private Long consultationId;
    
    // Participants
    private ParticipantDTO customer;
    private ParticipantDTO technician;
    
    // Last message preview
    private MessagePreviewDTO lastMessage;
    
    // Unread message count for current user
    private Integer unreadCount;
    
    /**
     * Nested DTO for conversation participants
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantDTO {
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private String phoneNumber;
        private Boolean isOnline;
    }
    
    /**
     * Nested DTO for last message preview
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessagePreviewDTO {
        private Long id;
        private String content;
        private String messageType;
        private String senderName;
        private LocalDateTime sentAt;
        private Boolean isRead;
    }
} 