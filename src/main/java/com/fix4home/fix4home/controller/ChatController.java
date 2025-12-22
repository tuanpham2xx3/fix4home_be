package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.chat.ConversationDTO;
import com.fix4home.fix4home.model.dto.chat.CreateConversationRequest;
import com.fix4home.fix4home.model.dto.chat.CreateFreeConversationRequest;
import com.fix4home.fix4home.model.dto.chat.MessageDTO;
import com.fix4home.fix4home.model.dto.chat.MarkAsReadRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.ChatService;
import com.fix4home.fix4home.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for chat management
 * Provides HTTP endpoints for conversation and message management
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Controller", description = "Chat and messaging management APIs")
public class ChatController {

    private final ConversationService conversationService;
    private final ChatService chatService;

    // ==================== CONVERSATION MANAGEMENT ====================

    @GetMapping("/conversations")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get user conversations", 
               description = "Get all conversations for the current user")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getUserConversations() {
        log.info("Getting conversations for current user");
        
        List<ConversationDTO> conversations = conversationService.getUserConversations();
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversations retrieved successfully", conversations));
    }

    @GetMapping("/conversations/paginated")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get user conversations with pagination", 
               description = "Get paginated conversations for the current user")
    public ResponseEntity<ApiResponse<Page<ConversationDTO>>> getUserConversationsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("Getting paginated conversations for current user: page={}, size={}", page, size);
        
        Page<ConversationDTO> conversations = conversationService.getUserConversationsPaginated(page, size);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversations retrieved successfully", conversations));
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get conversation by ID", 
               description = "Get detailed conversation information")
    public ResponseEntity<ApiResponse<ConversationDTO>> getConversationById(
            @Parameter(description = "Conversation ID") @PathVariable Long id) {
        log.info("Getting conversation with ID: {}", id);
        
        ConversationDTO conversation = conversationService.getConversationById(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversation retrieved successfully", conversation));
    }

    @PostMapping("/conversations")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Create new conversation", 
               description = "Create a new conversation (for free chat or business context)")
    public ResponseEntity<ApiResponse<ConversationDTO>> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {
        log.info("Creating new conversation between customer {} and technician {}", 
                request.getCustomerId(), request.getTechnicianId());
        
        ConversationDTO conversation = conversationService.createConversation(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversation created successfully", conversation));
    }
    
    @PostMapping("/conversations/free")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Create free conversation", 
               description = "Create a free chat conversation with another user")
    public ResponseEntity<ApiResponse<ConversationDTO>> createFreeConversation(
            @Valid @RequestBody CreateFreeConversationRequest request) {
        log.info("Creating free conversation with user {}", request.getOtherUserId());
        
        ConversationDTO conversation = conversationService.createFreeConversation(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Free conversation created successfully", conversation));
    }
    
    @GetMapping("/conversations/with/{userId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get conversation with user", 
               description = "Get existing conversation with a specific user, or null if not exists")
    public ResponseEntity<ApiResponse<ConversationDTO>> getConversationWithUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Getting conversation with user {}", userId);
        
        java.util.Optional<ConversationDTO> conversation = 
            conversationService.getExistingConversationWithUser(userId);
        
        if (conversation.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Conversation found", conversation.get()));
        } else {
            return ResponseEntity.ok(
                    ApiResponse.success("No conversation found", null));
        }
    }
    
    @PostMapping("/conversations/find-or-create")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Find or create conversation", 
               description = "Find existing conversation with user, or create new one if not exists")
    public ResponseEntity<ApiResponse<ConversationDTO>> findOrCreateConversation(
            @Parameter(description = "Other user ID") @RequestParam Long otherUserId) {
        log.info("Finding or creating conversation with user {}", otherUserId);
        
        ConversationDTO conversation = conversationService.findOrCreateConversation(otherUserId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversation retrieved or created successfully", conversation));
    }

    @PutMapping("/conversations/{id}/archive")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Archive conversation", 
               description = "Archive a conversation to make it read-only")
    public ResponseEntity<ApiResponse<ConversationDTO>> archiveConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long id) {
        log.info("Archiving conversation with ID: {}", id);
        
        ConversationDTO conversation = conversationService.archiveConversation(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversation archived successfully", conversation));
    }

    // ==================== MESSAGE MANAGEMENT ====================

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get conversation messages", 
               description = "Get paginated messages for a conversation")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getConversationMessages(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting messages for conversation: {}, page: {}, size: {}", conversationId, page, size);
        
        Page<MessageDTO> messages = chatService.getConversationMessages(conversationId, page, size);
        
        return ResponseEntity.ok(
                ApiResponse.success("Messages retrieved successfully", messages));
    }

    @GetMapping("/conversations/{conversationId}/messages/before")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get messages before timestamp", 
               description = "Get messages before a specific timestamp (for infinite scroll)")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getMessagesBefore(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Parameter(description = "Timestamp to load messages before") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
            @Parameter(description = "Number of messages to load") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting messages before {} for conversation: {}", beforeDate, conversationId);
        
        Page<MessageDTO> messages = chatService.getMessagesBefore(conversationId, beforeDate, size);
        
        return ResponseEntity.ok(
                ApiResponse.success("Messages retrieved successfully", messages));
    }

    @PostMapping("/messages/mark-read")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Mark messages as read", 
               description = "Mark specific messages or all messages in a conversation as read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @Valid @RequestBody MarkAsReadRequest request) {
        log.info("Marking messages as read for conversation: {}", request.getConversationId());
        
        chatService.markMessagesAsRead(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Messages marked as read successfully", null));
    }

    @GetMapping("/messages/unread")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get unread messages", 
               description = "Get all unread messages for the current user")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getUnreadMessages() {
        log.info("Getting unread messages for current user");
        
        List<MessageDTO> unreadMessages = chatService.getUnreadMessagesForUser();
        
        return ResponseEntity.ok(
                ApiResponse.success("Unread messages retrieved successfully", unreadMessages));
    }
} 