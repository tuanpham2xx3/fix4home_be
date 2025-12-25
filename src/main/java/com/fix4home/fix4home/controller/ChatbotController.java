package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.chatbot.ChatbotHistoryDTO;
import com.fix4home.fix4home.model.dto.chatbot.ChatbotMessageResponse;
import com.fix4home.fix4home.model.dto.chatbot.SendChatbotMessageRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for chatbot management
 * Provides HTTP endpoints for chatbot interactions with n8n
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot Controller", description = "Chatbot APIs for n8n integration")
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * Send a message to the chatbot
     * POST /api/v1/chatbot/send
     */
    @PostMapping("/send")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Send message to chatbot", 
               description = "Send a message to the chatbot and receive a response. " +
                           "Session will be automatically created or reused. " +
                           "After 5 message pairs, a new session will be created automatically.")
    public ResponseEntity<ApiResponse<ChatbotMessageResponse>> sendMessage(
            @Valid @RequestBody SendChatbotMessageRequest request) {
        log.info("Sending message to chatbot");
        
        try {
            Long userId = chatbotService.getCurrentUserId();
            ChatbotMessageResponse response = chatbotService.sendMessage(userId, request.getMessage());
            
            return ResponseEntity.ok(
                    ApiResponse.success("Message sent successfully", response));
        } catch (Exception e) {
            log.error("Error sending message to chatbot: {}", e.getMessage(), e);
            // Exception will be handled by GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * Get chat history for the current user
     * GET /api/v1/chatbot/history
     */
    @GetMapping("/history")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get chat history", 
               description = "Get paginated chat history for the current user")
    public ResponseEntity<ApiResponse<Page<ChatbotHistoryDTO>>> getChatHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting chat history for current user: page={}, size={}", page, size);
        
        Long userId = chatbotService.getCurrentUserId();
        Page<ChatbotHistoryDTO> history = chatbotService.getChatHistory(userId, page, size);
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat history retrieved successfully", history));
    }

    /**
     * Get chat history for a specific session
     * GET /api/v1/chatbot/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get session chat history", 
               description = "Get all messages for a specific chatbot session")
    public ResponseEntity<ApiResponse<java.util.List<ChatbotHistoryDTO>>> getSessionHistory(
            @Parameter(description = "Session ID (8 digits)") @PathVariable String sessionId) {
        log.info("Getting chat history for session: {}", sessionId);
        
        Long userId = chatbotService.getCurrentUserId();
        java.util.List<ChatbotHistoryDTO> history = chatbotService.getSessionHistory(userId, sessionId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Session history retrieved successfully", history));
    }
}

