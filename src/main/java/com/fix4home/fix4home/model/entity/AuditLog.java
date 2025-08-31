package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing audit logs and request tracking
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 100)
    private String username;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "resource", length = 200)
    private String resource;
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(name = "method", length = 10)
    private String method;
    
    @Column(name = "endpoint", length = 500)
    private String endpoint;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "processing_time")
    private Long processingTime; // in milliseconds
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Indexes for better query performance
    @Table(indexes = {
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_action", columnList = "action"),
        @Index(name = "idx_audit_logs_resource", columnList = "resource"),
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_logs_ip_address", columnList = "ip_address"),
        @Index(name = "idx_audit_logs_endpoint", columnList = "endpoint")
    })
    public static class AuditLogTable {}
}
