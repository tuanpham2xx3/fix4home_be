package com.fix4home.fix4home.model.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for chatbot send message endpoint
 * Contains sessionId (may be new if limit exceeded) and message information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSendResponse {
    
    /**
     * Session ID used for n8n node memory
     * This may be a new sessionId if the previous one exceeded the limit (5 requests)
     */
    private String sessionId;
    
    /**
     * ID of the saved message in database
     */
    private Long messageId;
    
    /**
     * Whether the message was successfully sent to n8n
     */
    private Boolean messageSent;
    
    /**
     * Whether a new sessionId was created (because previous session exceeded limit)
     */
    private Boolean newSessionCreated;
}

