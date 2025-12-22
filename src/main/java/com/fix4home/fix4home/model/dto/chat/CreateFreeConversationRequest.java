package com.fix4home.fix4home.model.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating free chat conversations
 * Used when users want to chat with each other without business context
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFreeConversationRequest {
    
    @NotNull(message = "Other user ID is required")
    private Long otherUserId;
    
    // Optional initial message
    private String initialMessage;
}

