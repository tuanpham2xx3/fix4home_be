package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when payment processing fails
 */
public class PaymentProcessingException extends BaseBusinessException {
    
    public PaymentProcessingException(String reason) {
        super(
            "PAYMENT_PROCESSING_FAILED",
            "Payment processing failed: " + reason,
            "Payment could not be processed. Please try again or use a different payment method.",
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
    
    public PaymentProcessingException(String reason, Throwable cause) {
        super(
            "PAYMENT_PROCESSING_FAILED",
            "Payment processing failed: " + reason,
            "Payment could not be processed. Please try again or use a different payment method.",
            HttpStatus.UNPROCESSABLE_ENTITY,
            null,
            cause
        );
    }
    
    public static PaymentProcessingException cardDeclined() {
        return new PaymentProcessingException("Card was declined by the bank");
    }
    
    public static PaymentProcessingException insufficientFunds() {
        return new PaymentProcessingException("Insufficient funds in account");
    }
    
    public static PaymentProcessingException expiredCard() {
        return new PaymentProcessingException("Payment card has expired");
    }
} 