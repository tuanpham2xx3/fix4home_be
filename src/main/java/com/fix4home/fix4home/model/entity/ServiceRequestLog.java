package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_request_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private ServiceRequestStatus oldStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ServiceRequestStatus newStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;
    
    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;
} 