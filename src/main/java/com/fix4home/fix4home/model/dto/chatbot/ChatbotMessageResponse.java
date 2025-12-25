package com.fix4home.fix4home.model.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO after sending a message to the chatbot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessageResponse {
    
    /**
     * The bot's response message
     */
    private String output;
    
    /**
     * Current sessionId being used
     */
    private String sessionId;
    
    /**
     * Number of message pairs in current session
     */
    private Integer messageCount;
    
    /**
     * Whether a new session was created
     */
    private Boolean newSession;
    
    /**
     * Timestamp of the response
     */
    private LocalDateTime timestamp;
}

