package com.fix4home.fix4home.model.dto.user;

import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for searching users
 * Used to find users for chat functionality
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {
    
    // Search keyword (username, email)
    private String keyword;
    
    // Filter by role
    private Role role;
    
    // Filter by status (default: ACTIVE)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    // Pagination
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
}

