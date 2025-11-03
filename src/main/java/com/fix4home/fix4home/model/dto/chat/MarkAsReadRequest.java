package com.fix4home.fix4home.model.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for marking messages as read
 * Used to track message read status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkAsReadRequest {
    
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
    
    // Optional: specific message IDs to mark as read
    // If null, mark all unread messages in conversation as read
    private List<Long> messageIds;
} 