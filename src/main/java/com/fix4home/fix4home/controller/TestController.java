package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.security.SecurityConstants;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Fix4Home Backend is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return response;
    }

    @GetMapping("/database")
    public Map<String, Object> databaseCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("database", "MySQL");
        response.put("connection", "Ready");
        response.put("message", "Database connection is configured");
        return response;
    }

    @GetMapping("/customer")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    public Map<String, Object> customerEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer access granted");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/technician")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    public Map<String, Object> technicianEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Technician access granted");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/admin")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    public Map<String, Object> adminEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/any-role")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    public Map<String, Object> anyRoleEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authenticated user access granted");
        response.put("user", authentication.getName());
        response.put("role", authentication.getAuthorities());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
} 