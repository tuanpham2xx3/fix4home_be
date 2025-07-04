package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.LoginRequest;
import com.fix4home.fix4home.model.dto.auth.RegisterRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.service.AuthService;
import com.fix4home.fix4home.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new customer, technician, or admin")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        
        String message = switch (request.getRole()) {
            case CUSTOMER -> "Customer registered successfully";
            case TECHNICIAN -> "Technician registered successfully. Awaiting admin approval.";
            case ADMIN -> "Admin registered successfully";
        };
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout user (client-side token removal)")
    public ResponseEntity<ApiResponse<String>> logout() {
        // JWT is stateless, so logout is handled client-side by removing the token
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
} 