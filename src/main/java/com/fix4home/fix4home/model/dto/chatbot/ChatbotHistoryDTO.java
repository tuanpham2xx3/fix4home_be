package com.fix4home.fix4home.model.dto.chatbot;

import com.fix4home.fix4home.model.enums.ChatbotMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for chatbot message history
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotHistoryDTO {
    
    private Long id;
    private String sessionId;
    private String message;
    private ChatbotMessageType messageType;
    private Integer pairSequence;
    private LocalDateTime createdAt;
}

