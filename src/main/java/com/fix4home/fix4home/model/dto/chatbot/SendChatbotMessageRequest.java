package com.fix4home.fix4home.model.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending a message to the chatbot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendChatbotMessageRequest {
    
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String message;
    
    /**
     * Optional sessionId - if not provided, system will create/get one
     * If provided, must be valid 8-digit sessionId
     */
    @Size(min = 8, max = 8, message = "SessionId must be exactly 8 digits")
    private String sessionId;
}

