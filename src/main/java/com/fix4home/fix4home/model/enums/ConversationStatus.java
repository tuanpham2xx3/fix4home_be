package com.fix4home.fix4home.model.enums;

/**
 * Represents the status of a conversation in the chat system
 * Used to manage conversation lifecycle and access control
 */
public enum ConversationStatus {
    /**
     * Active conversation - users can send and receive messages
     */
    ACTIVE,
    
    /**
     * Archived conversation - read-only, typically after service completion
     */
    ARCHIVED,
    
    /**
     * Blocked conversation - communication disabled due to policy violation
     */
    BLOCKED
} 