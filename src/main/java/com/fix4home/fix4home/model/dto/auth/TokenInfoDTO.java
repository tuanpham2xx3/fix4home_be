package com.fix4home.fix4home.model.dto.auth;

import com.fix4home.fix4home.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfoDTO {
    private Long userId;
    private String username;
    private Role role;
    private Long expiresIn; // Thời gian còn lại của token (tính bằng giây)
    private boolean isValid;
} 