package com.fix4home.fix4home.model.dto.user;

import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result DTO for user search
 * Contains user information and conversation status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResultDTO {
    
    private Long id;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    
    // Indicates if there's an existing conversation with this user
    private Boolean hasExistingConversation;
    
    // Conversation ID if exists
    private Long conversationId;
}

