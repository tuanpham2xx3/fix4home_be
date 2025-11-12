package com.fix4home.fix4home.model.dto.customer;

import com.fix4home.fix4home.model.enums.Gender;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileDTO {
    
    // User basic info
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Customer profile info
    private Long profileId;
    private String fullName;
    private Gender gender;
    private LocalDate dob;
    private String avatarUrl;
} 