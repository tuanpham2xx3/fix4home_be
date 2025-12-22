package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.user.UserSearchRequest;
import com.fix4home.fix4home.model.dto.user.UserSearchResultDTO;
import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.UserSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for user search functionality
 * Used to find users for chat
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Search Controller", description = "User search APIs for chat functionality")
public class UserSearchController {
    
    private final UserSearchService userSearchService;
    
    @GetMapping("/search")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Search users", 
               description = "Search users by keyword, role, or status for chat functionality")
    public ResponseEntity<ApiResponse<List<UserSearchResultDTO>>> searchUsers(
            @Parameter(description = "Search keyword (username)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Searching users: keyword={}, role={}, status={}, page={}, size={}", 
            keyword, role, status, page, size);
        
        UserSearchRequest request = UserSearchRequest.builder()
                .keyword(keyword)
                .role(role != null ? com.fix4home.fix4home.model.enums.Role.valueOf(role) : null)
                .status(status != null ? com.fix4home.fix4home.model.enums.UserStatus.valueOf(status) : null)
                .page(page)
                .size(size)
                .build();
        
        List<UserSearchResultDTO> results = userSearchService.searchUsers(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", results));
    }
    
    @GetMapping("/chat-eligible")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get chat-eligible users", 
               description = "Get list of users available for chat")
    public ResponseEntity<ApiResponse<List<UserSearchResultDTO>>> getChatEligibleUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting chat-eligible users: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        List<UserSearchResultDTO> results = userSearchService.getUsersForChat(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat-eligible users retrieved successfully", results));
    }
    
    @GetMapping("/{userId}/conversation")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get existing conversation with user", 
               description = "Check if conversation exists with a specific user")
    public ResponseEntity<ApiResponse<Conversation>> getExistingConversation(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Checking existing conversation with user {}", userId);
        
        Optional<Conversation> conversation = userSearchService.getExistingConversationWithUser(userId);
        
        if (conversation.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Conversation found", conversation.get()));
        } else {
            return ResponseEntity.ok(
                    ApiResponse.success("No conversation found", null));
        }
    }
}

