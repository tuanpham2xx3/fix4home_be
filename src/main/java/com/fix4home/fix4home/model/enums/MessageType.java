package com.fix4home.fix4home.model.enums;

/**
 * Represents different types of messages in the chat system
 * Used to handle different message content and rendering
 */
public enum MessageType {
    /**
     * Plain text message - regular conversation
     */
    TEXT,
    
    /**
     * Image attachment - photos of problems, solutions, etc.
     */
    IMAGE,
    
    /**
     * Location sharing - coordinates and address information
     */
    LOCATION,
    
    /**
     * System generated message - automated notifications
     */
    SYSTEM,
    
    /**
     * Price quotation message - technician proposals and pricing
     */
    QUOTATION,
    
    /**
     * File attachment - documents, receipts, etc.
     */
    FILE
} 