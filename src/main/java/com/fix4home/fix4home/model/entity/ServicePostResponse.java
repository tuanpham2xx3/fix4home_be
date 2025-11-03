package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_post_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePostResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_post_id", nullable = false)
    @NotNull(message = "Service post is required")
    private ServicePost servicePost;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    @NotNull(message = "Technician is required")
    private User technician;
    
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Message is required")
    private String message;
    
    @Column(name = "quoted_price", precision = 12, scale = 2)
    @PositiveOrZero(message = "Quoted price must be positive or zero")
    private BigDecimal quotedPrice;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes
    
    @Column(name = "proposed_time")
    private LocalDateTime proposedTime;
    
    @Column(name = "is_selected")
    @Builder.Default
    private Boolean isSelected = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Business logic methods
    public boolean isSelected() {
        return Boolean.TRUE.equals(isSelected);
    }
    
    public String getTechnicianName() {
        // Will need to get name from TechnicianProfile in service layer
        return technician != null ? technician.getUsername() : null;
    }
} 