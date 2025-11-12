package com.fix4home.fix4home.model.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
    
    // Main field - accepts usernameOrEmail, email, or username from JSON
    // Use @JsonAlias to accept multiple field names (primary field is usernameOrEmail)
    @JsonAlias({"email", "username"})
    private String usernameOrEmail;
    
    // Password field
    private String password;
    
    // Getter that trims the value
    public String getUsernameOrEmail() {
        return usernameOrEmail != null ? usernameOrEmail.trim() : null;
    }
} 