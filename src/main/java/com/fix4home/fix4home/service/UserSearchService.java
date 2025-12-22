package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.dto.user.UserSearchRequest;
import com.fix4home.fix4home.model.dto.user.UserSearchResultDTO;
import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.ConversationRepository;
import com.fix4home.fix4home.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for searching users for chat functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService extends BaseService {
    
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    
    @Transactional(readOnly = true)
    public List<UserSearchResultDTO> searchUsers(UserSearchRequest request) {
        logBusinessOperation("SEARCH_USERS", "keyword=" + request.getKeyword() + ", role=" + request.getRole());
        
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        Page<User> usersPage;
        
        // Build query based on filters
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            // Search by keyword (username)
            usersPage = userRepository.findByUsernameContainingIgnoreCase(
                request.getKeyword().trim(), pageable);
        } else if (request.getRole() != null) {
            // Filter by role and status
            usersPage = userRepository.findByRoleAndStatus(
                request.getRole(), request.getStatus(), pageable);
        } else {
            // Get all active users except current user
            usersPage = userRepository.findByIdNotAndStatus(
                currentUser.getId(), request.getStatus(), pageable);
        }
        
        // Convert to DTOs and check for existing conversations
        return usersPage.getContent().stream()
                .filter(user -> !user.getId().equals(currentUser.getId())) // Exclude current user
                .map(user -> convertToSearchResultDTO(user, currentUser))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserSearchResultDTO> getUsersForChat(Pageable pageable) {
        logBusinessOperation("GET_USERS_FOR_CHAT");
        
        User currentUser = getCurrentUser();
        
        // Get active users except current user
        Page<User> usersPage = userRepository.findByIdNotAndStatus(
            currentUser.getId(), UserStatus.ACTIVE, pageable);
        
        return usersPage.getContent().stream()
                .map(user -> convertToSearchResultDTO(user, currentUser))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<Conversation> getExistingConversationWithUser(Long userId) {
        User currentUser = getCurrentUser();
        return conversationRepository.findByParticipants(currentUser.getId(), userId);
    }
    
    private UserSearchResultDTO convertToSearchResultDTO(User user, User currentUser) {
        // Check if conversation exists
        Optional<Conversation> existingConversation = conversationRepository.findByParticipants(
            currentUser.getId(), user.getId());
        
        return UserSearchResultDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .hasExistingConversation(existingConversation.isPresent())
                .conversationId(existingConversation.map(Conversation::getId).orElse(null))
                .build();
    }
}

