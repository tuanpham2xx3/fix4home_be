package com.fix4home.fix4home.model.dto.chatbot;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for chatbot webhook from n8n
 * Receives chatbot responses from n8n workflow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotWebhookRequest {
    
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
    
    @NotNull(message = "Response is required")
    private String response;
    
    // Optional metadata from n8n
    private String metadata;
}

