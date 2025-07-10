package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ConsultationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {
    
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
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Proposal is required")
    private String proposal;
    
    @Column(name = "quoted_price", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "Quoted price is required")
    @PositiveOrZero(message = "Quoted price must be positive or zero")
    private BigDecimal quotedPrice;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    // Business logic methods
    public boolean isPending() {
        return status == ConsultationStatus.PENDING;
    }
    
    public boolean isAccepted() {
        return status == ConsultationStatus.ACCEPTED;
    }
    
    public boolean isRejected() {
        return status == ConsultationStatus.REJECTED;
    }
    
    public boolean canBeModified() {
        return status == ConsultationStatus.PENDING;
    }
    
    public void accept() {
        this.status = ConsultationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void reject() {
        this.status = ConsultationStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }
} 