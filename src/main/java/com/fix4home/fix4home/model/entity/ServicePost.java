package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.model.enums.ServicePostType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @NotNull(message = "Service is required")
    private Service service;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    @NotNull(message = "Address is required")
    private Address address;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    private String description;
    
    @Column(name = "estimated_budget", precision = 12, scale = 2)
    @PositiveOrZero(message = "Estimated budget must be positive or zero")
    private BigDecimal estimatedBudget;
    
    @Column(name = "preferred_time")
    private LocalDateTime preferredTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ServicePostType type = ServicePostType.SCHEDULED;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ServicePostStatus status = ServicePostStatus.DRAFT;
    
    @Column(name = "max_technicians")
    @Positive(message = "Max technicians must be positive")
    @Builder.Default
    private Integer maxTechnicians = 5;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_technician_id")
    private User selectedTechnician;
    
    @Column(name = "selected_at")
    private LocalDateTime selectedAt;
    
    @Column(name = "final_price", precision = 12, scale = 2)
    @PositiveOrZero(message = "Final price must be positive or zero")
    private BigDecimal finalPrice;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-many relationship with ServicePostResponse
    @OneToMany(mappedBy = "servicePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServicePostResponse> responses = new ArrayList<>();
    
    // Business logic methods
    public boolean isActive() {
        return status == ServicePostStatus.POSTED || 
               status == ServicePostStatus.RESPONSES_RECEIVED;
    }
    
    public boolean canReceiveResponses() {
        return isActive() && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now())) &&
               (maxTechnicians == null || responses.size() < maxTechnicians);
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public int getResponseCount() {
        return responses != null ? responses.size() : 0;
    }
} 