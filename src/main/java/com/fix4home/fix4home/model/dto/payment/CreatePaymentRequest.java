package com.fix4home.fix4home.model.dto.payment;

import com.fix4home.fix4home.model.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    
    @NotNull(message = "Service request ID is required")
    private Long serviceRequestId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
    
    // Additional payment info
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private String bankAccount;
    private String bankName;
    private String notes;
} 