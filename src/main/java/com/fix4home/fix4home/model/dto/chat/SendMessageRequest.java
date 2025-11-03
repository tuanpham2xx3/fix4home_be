package com.fix4home.fix4home.model.dto.chat;

import com.fix4home.fix4home.model.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending messages
 * Used in both WebSocket and REST API endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
    
    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 5000, message = "Message content cannot exceed 5000 characters")
    private String content;
    
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    
    private String attachmentUrl;
    
    private String metadata;
} 