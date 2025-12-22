package com.fix4home.fix4home.model.enums;

/**
 * Represents the type of conversation in the chat system
 * Used to distinguish between business-related and free chat conversations
 */
public enum ConversationType {
    /**
     * Business conversation - linked to ServiceRequest, ServicePost, or Consultation
     */
    BUSINESS,
    
    /**
     * Free chat conversation - between users without business context
     */
    FREE,
    
    /**
     * Chatbot conversation - between user and chatbot support
     */
    CHATBOT
}

