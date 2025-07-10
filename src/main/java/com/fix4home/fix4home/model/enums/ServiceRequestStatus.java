package com.fix4home.fix4home.model.enums;

public enum ServiceRequestStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    DONE,
    CANCELLED,
    COMPLAINING,  // Service request is under complaint/dispute
    COMPLAITED    // Complaint has been processed (resolved or rejected)
} 