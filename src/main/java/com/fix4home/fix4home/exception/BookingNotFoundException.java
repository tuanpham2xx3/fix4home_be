package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a booking is not found
 */
public class BookingNotFoundException extends BaseBusinessException {
    
    public BookingNotFoundException(Long bookingId) {
        super(
            "BOOKING_NOT_FOUND",
            "Booking not found with ID: " + bookingId,
            "The requested booking was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public BookingNotFoundException(Long bookingId, Long userId) {
        super(
            "BOOKING_NOT_FOUND",
            "Booking ID " + bookingId + " not found for user ID " + userId,
            "You don't have access to this booking or it doesn't exist.",
            HttpStatus.NOT_FOUND
        );
    }
}

