package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Address is required")
    private String address;
    
    @Column(nullable = false)
    @NotNull(message = "Date is required")
    private LocalDateTime date;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Phone is required")
    private String phone;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Name is required")
    private String name;
    
    @Column(name = "ward_code", length = 20)
    private String wardCode;
    
    @Column(name = "needs_survey", nullable = false)
    @Builder.Default
    private Boolean needsSurvey = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

