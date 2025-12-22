package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.chat.MessageDTO;
import com.fix4home.fix4home.model.dto.chatbot.ChatbotWebhookRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.entity.Message;
import com.fix4home.fix4home.service.ChatService;
import com.fix4home.fix4home.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for receiving chatbot webhook responses from n8n
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot Webhook Controller", description = "Webhook endpoint for n8n chatbot responses")
public class ChatbotWebhookController {
    
    private final ChatbotService chatbotService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/webhook")
    @Operation(summary = "Receive chatbot response from n8n", 
               description = "Webhook endpoint to receive chatbot responses from n8n workflow")
    public ResponseEntity<ApiResponse<MessageDTO>> receiveChatbotResponse(
            @Valid @RequestBody ChatbotWebhookRequest request) {
        log.info("Received chatbot response for conversation: {}", request.getConversationId());
        
        // Process chatbot response and create message
        Message message = chatbotService.processChatbotResponse(
            request.getConversationId(), 
            request.getResponse(),
            request.getMetadata());
        
        // Convert to DTO
        MessageDTO messageDTO = chatService.convertMessageToDTO(message);
        
        // Broadcast to user via WebSocket
        // Get conversation to find recipient
        com.fix4home.fix4home.model.entity.Conversation conversation = 
            message.getConversation();
        
        // Find the other participant (not chatbot)
        com.fix4home.fix4home.model.entity.User recipient = 
            chatbotService.isChatbotUser(conversation.getCustomer())
                ? conversation.getTechnician() 
                : conversation.getCustomer();
        
        // Send via WebSocket
        messagingTemplate.convertAndSendToUser(
            recipient.getUsername(),
            "/queue/messages",
            messageDTO);
        
        log.info("Chatbot response sent to user: {}", recipient.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.success("Chatbot response processed successfully", messageDTO));
    }
}

