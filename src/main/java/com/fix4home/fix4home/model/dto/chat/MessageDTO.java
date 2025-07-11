package com.fix4home.fix4home.model.dto.chat;

import com.fix4home.fix4home.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Message entity
 * Used for API responses and WebSocket message transmission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    
    private Long id;
    private Long conversationId;
    private String content;
    private MessageType messageType;
    private String attachmentUrl;
    private String metadata;
    private Boolean isRead;
    private LocalDateTime sentAt;
    
    // Sender information
    private SenderDTO sender;
    
    /**
     * Nested DTO for message sender information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SenderDTO {
        private Long userId;
        private String username;
        private String fullName;
        private String role;
    }
} 