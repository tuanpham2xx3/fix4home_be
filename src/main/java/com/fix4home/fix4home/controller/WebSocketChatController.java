package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.chat.MessageDTO;
import com.fix4home.fix4home.model.dto.chat.SendMessageRequest;
import com.fix4home.fix4home.model.dto.chat.TypingIndicatorDTO;
import com.fix4home.fix4home.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket Controller for real-time chat functionality
 * Handles WebSocket messages using STOMP protocol
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle message sending via WebSocket
     * Messages are sent to /app/chat.send and broadcast to conversation participants
     */
    @MessageMapping("/chat.send")
    @SendToUser("/queue/reply")
    public MessageDTO sendMessage(@Payload SendMessageRequest request, Principal principal) {
        log.info("Received WebSocket message from user: {} for conversation: {}", 
                principal.getName(), request.getConversationId());
        
        try {
            // Send message using chat service
            MessageDTO messageDTO = chatService.sendMessage(request, principal.getName());
            
            log.debug("Message sent successfully via WebSocket: {}", messageDTO.getId());
            return messageDTO;
            
        } catch (Exception e) {
            log.error("Error sending message via WebSocket: {}", e.getMessage());
            // Return error message to sender
            return MessageDTO.builder()
                    .content("Error sending message: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Handle typing indicator
     * Shows when users are typing in a conversation
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicatorDTO typingIndicator, Principal principal) {
        log.debug("Received typing indicator from user: {} for conversation: {}", 
                principal.getName(), typingIndicator.getConversationId());
        
        try {
            // Update typing indicator with current user info
            typingIndicator.setUsername(principal.getName());
            
            // Broadcast typing indicator to conversation participants
            String destination = "/topic/conversation/" + typingIndicator.getConversationId() + "/typing";
            messagingTemplate.convertAndSend(destination, typingIndicator);
            
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Handle user joining a conversation
     * Notifies other participants about user presence
     */
    @MessageMapping("/chat.join")
    public void handleJoinConversation(@Payload JoinConversationRequest request, Principal principal) {
        log.info("User {} joined conversation: {}", principal.getName(), request.getConversationId());
        
        try {
            // Create join notification
            ConversationEventDTO joinEvent = ConversationEventDTO.builder()
                    .conversationId(request.getConversationId())
                    .username(principal.getName())
                    .eventType("USER_JOINED")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            
            // Broadcast to conversation topic
            String destination = "/topic/conversation/" + request.getConversationId() + "/events";
            messagingTemplate.convertAndSend(destination, joinEvent);
            
        } catch (Exception e) {
            log.error("Error handling join conversation: {}", e.getMessage());
        }
    }

    /**
     * Handle user leaving a conversation
     * Notifies other participants about user departure
     */
    @MessageMapping("/chat.leave")
    public void handleLeaveConversation(@Payload LeaveConversationRequest request, Principal principal) {
        log.info("User {} left conversation: {}", principal.getName(), request.getConversationId());
        
        try {
            // Create leave notification
            ConversationEventDTO leaveEvent = ConversationEventDTO.builder()
                    .conversationId(request.getConversationId())
                    .username(principal.getName())
                    .eventType("USER_LEFT")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            
            // Broadcast to conversation topic
            String destination = "/topic/conversation/" + request.getConversationId() + "/events";
            messagingTemplate.convertAndSend(destination, leaveEvent);
            
        } catch (Exception e) {
            log.error("Error handling leave conversation: {}", e.getMessage());
        }
    }

    // ==================== NESTED DTOs ====================

    /**
     * DTO for joining conversation requests
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class JoinConversationRequest {
        private Long conversationId;
    }

    /**
     * DTO for leaving conversation requests
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class LeaveConversationRequest {
        private Long conversationId;
    }

    /**
     * DTO for conversation events (join, leave, etc.)
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ConversationEventDTO {
        private Long conversationId;
        private String username;
        private String eventType;
        private java.time.LocalDateTime timestamp;
    }
} 