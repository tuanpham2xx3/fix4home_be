package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ComplaintStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    @NotNull(message = "Service request is required")
    private ServiceRequest serviceRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complainant_id", nullable = false)
    @NotNull(message = "Complainant is required")
    private User complainant; // Người khiếu nại
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accused_id", nullable = false)
    @NotNull(message = "Accused party is required")
    private User accused; // Người bị khiếu nại
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Complaint reason is required")
    @Size(min = 10, max = 200, message = "Reason must be between 10 and 200 characters")
    private String reason; // Lý do khiếu nại
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Complaint description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description; // Mô tả chi tiết
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.PENDING;
    
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse; // Phản hồi từ admin
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy; // Admin xử lý
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    // Business logic methods
    public boolean isPending() {
        return status == ComplaintStatus.PENDING;
    }
    
    public boolean isInvestigating() {
        return status == ComplaintStatus.INVESTIGATING;
    }
    
    public boolean isResolved() {
        return status == ComplaintStatus.RESOLVED;
    }
    
    public boolean isRejected() {
        return status == ComplaintStatus.REJECTED;
    }
    
    public boolean canBeModified() {
        return status == ComplaintStatus.PENDING;
    }
    
    public boolean canBeInvestigated() {
        return status == ComplaintStatus.PENDING;
    }
    
    public boolean canBeResolved() {
        return status == ComplaintStatus.INVESTIGATING;
    }
    
    public void startInvestigation(User admin) {
        this.status = ComplaintStatus.INVESTIGATING;
        this.resolvedBy = admin;
    }
    
    public void resolve(String adminResponse, User admin) {
        this.status = ComplaintStatus.RESOLVED;
        this.adminResponse = adminResponse;
        this.resolvedBy = admin;
        this.resolvedAt = LocalDateTime.now();
    }
    
    public void reject(String adminResponse, User admin) {
        this.status = ComplaintStatus.REJECTED;
        this.adminResponse = adminResponse;
        this.resolvedBy = admin;
        this.resolvedAt = LocalDateTime.now();
    }
} 