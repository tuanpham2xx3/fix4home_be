package com.fix4home.fix4home.model.dto.auth;

import com.fix4home.fix4home.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone number must be 10-11 digits")
    private String phoneNumber;  // Optional for CUSTOMER, required for TECHNICIAN (validated in service layer)
    
    @NotNull(message = "Role is required")
    private Role role;
    
    // Optional profile fields (optional for CUSTOMER, required for TECHNICIAN)
    private String fullName;
    
    // For technician registration
    private String skills;
    private String experience;

    private String adminKey; // Optional, required only for ADMIN registration
} 