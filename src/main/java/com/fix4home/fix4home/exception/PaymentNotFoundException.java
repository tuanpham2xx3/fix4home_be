package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a payment is not found
 */
public class PaymentNotFoundException extends BaseBusinessException {
    
    public PaymentNotFoundException(Long paymentId) {
        super(
            "PAYMENT_NOT_FOUND",
            "Payment not found with ID: " + paymentId,
            "The requested payment was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public PaymentNotFoundException(Long serviceRequestId, String context) {
        super(
            "PAYMENT_NOT_FOUND",
            "Payment not found for service request ID: " + serviceRequestId + " (" + context + ")",
            "No payment found for this service request.",
            HttpStatus.NOT_FOUND
        );
    }
} 