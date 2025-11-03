package com.fix4home.fix4home.exception;

/**
 * Exception thrown when a technician tries to respond to a service post they already responded to
 */
public class ServicePostAlreadyRespondedException extends BaseBusinessException {
    
    public ServicePostAlreadyRespondedException(Long servicePostId, Long technicianId) {
        super("Technician with ID " + technicianId + " has already responded to service post with ID " + servicePostId);
    }
    
    public ServicePostAlreadyRespondedException(String message) {
        super(message);
    }
    
    public static ServicePostAlreadyRespondedException withIds(Long servicePostId, Long technicianId) {
        return new ServicePostAlreadyRespondedException(servicePostId, technicianId);
    }
} 