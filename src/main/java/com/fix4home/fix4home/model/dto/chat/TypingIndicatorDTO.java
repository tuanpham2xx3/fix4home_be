package com.fix4home.fix4home.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for typing indicator functionality
 * Sent via WebSocket to show when users are typing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingIndicatorDTO {
    
    private Long conversationId;
    private String username;
    private String fullName;
    private Boolean isTyping;
} 