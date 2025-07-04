package com.fix4home.fix4home.model.dto.payment;

import com.fix4home.fix4home.model.enums.PaymentMethod;
import com.fix4home.fix4home.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    
    private Long id;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paymentTime;
    
    // Service Request details
    private Long serviceRequestId;
    private String serviceRequestDescription;
    private String serviceName;
    
    // Customer details
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    // Technician details
    private Long technicianId;
    private String technicianName;
    private String technicianEmail;
    
    // UI fields
    private String formattedAmount;
    private String statusDisplay;
    private String methodDisplay;
    private String timeAgo;
    private boolean canCancel;
    private boolean canRefund;
} 