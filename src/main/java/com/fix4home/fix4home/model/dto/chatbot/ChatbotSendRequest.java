package com.fix4home.fix4home.model.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending message to n8n chatbot webhook with session management
 * Includes conversationId for message storage and sessionId for n8n node memory
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSendRequest {
    
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
    
    @NotBlank(message = "Message content cannot be empty")
    private String message;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}

