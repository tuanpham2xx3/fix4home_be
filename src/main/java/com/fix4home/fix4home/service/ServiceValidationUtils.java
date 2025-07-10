package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.experimental.UtilityClass;

/**
 * Utility class for common service validation patterns
 * Provides standardized validation methods for business rules
 */
@UtilityClass
public class ServiceValidationUtils {

    // ==================== USER VALIDATIONS ====================

    /**
     * Validate user exists and has expected role
     */
    public static void validateUserRole(User user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new BusinessValidationException(
                "Invalid user role. Expected: " + expectedRole + ", Actual: " + user.getRole()
            );
        }
    }

    /**
     * Validate user is active
     */
    public static void validateUserActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw AccountNotActiveException.withStatus(user.getStatus());
        }
    }

    /**
     * Validate user owns resource
     */
    public static void validateResourceOwnership(User user, User resourceOwner) {
        if (!user.getId().equals(resourceOwner.getId())) {
            throw new SecurityException("Access denied: Resource does not belong to user");
        }
    }

    // ==================== SERVICE VALIDATIONS ====================

    /**
     * Validate service is active and available
     */
    public static void validateServiceAvailable(Service service) {
        if (service.getStatus() != UserStatus.ACTIVE) {
            throw ServiceNotAvailableException.withId(service.getId());
        }
    }

    // ==================== SERVICE REQUEST VALIDATIONS ====================

    /**
     * Validate service request status transition
     */
    public static void validateStatusTransition(ServiceRequestStatus currentStatus, ServiceRequestStatus newStatus) {
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == ServiceRequestStatus.ASSIGNED || 
                           newStatus == ServiceRequestStatus.CANCELLED;
            case ASSIGNED -> newStatus == ServiceRequestStatus.IN_PROGRESS || 
                            newStatus == ServiceRequestStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == ServiceRequestStatus.DONE;
            case DONE, CANCELLED -> false; // Terminal states
        };

        if (!validTransition) {
            throw InvalidServiceRequestStatusException.invalidTransition(currentStatus, newStatus);
        }
    }

    /**
     * Validate service request can be cancelled
     */
    public static void validateCanCancel(ServiceRequest serviceRequest) {
        if (serviceRequest.getStatus() == ServiceRequestStatus.IN_PROGRESS ||
            serviceRequest.getStatus() == ServiceRequestStatus.DONE ||
            serviceRequest.getStatus() == ServiceRequestStatus.CANCELLED) {
            throw InvalidServiceRequestStatusException.cannotCancel(serviceRequest.getStatus());
        }
    }

    /**
     * Validate service request assignment
     */
    public static void validateServiceRequestAssignment(ServiceRequest serviceRequest, User technician) {
        // Check if request is in correct status
        if (serviceRequest.getStatus() != ServiceRequestStatus.PENDING) {
            throw InvalidServiceRequestStatusException.cannotAssign(serviceRequest.getStatus());
        }

        // Check if technician is valid
        validateUserRole(technician, Role.TECHNICIAN);
        validateUserActive(technician);

        // Check if already assigned
        if (serviceRequest.getTechnician() != null) {
            throw new BusinessValidationException("Service request is already assigned to another technician");
        }
    }

    // ==================== TECHNICIAN VALIDATIONS ====================

    /**
     * Validate technician is approved and active
     */
    public static void validateTechnicianApproved(TechnicianProfile technicianProfile) {
        if (technicianProfile.getStatus() != UserStatus.ACTIVE) {
            throw TechnicianNotAvailableException.withStatus(technicianProfile.getStatus());
        }
    }

    // ==================== ADDRESS VALIDATIONS ====================

    /**
     * Validate address belongs to user
     */
    public static void validateAddressOwnership(Address address, User user) {
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessValidationException("Address does not belong to current user");
        }
    }

    // ==================== PAYMENT VALIDATIONS ====================

    /**
     * Validate service request is eligible for payment
     */
    public static void validatePaymentEligibility(ServiceRequest serviceRequest) {
        if (serviceRequest.getStatus() != ServiceRequestStatus.DONE) {
            throw new BusinessValidationException("Can only create payment for completed service requests");
        }
    }

    /**
     * Validate payment does not already exist
     */
    public static void validateNoExistingPayment(boolean paymentExists) {
        if (paymentExists) {
            throw new BusinessValidationException("Payment already exists for this service request");
        }
    }

    // ==================== COMMON VALIDATIONS ====================

    /**
     * Validate pagination parameters
     */
    public static void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new BusinessValidationException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessValidationException("Page size must be between 1 and 100");
        }
    }

    /**
     * Validate sort direction
     */
    public static void validateSortDirection(String sortDir) {
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            throw new BusinessValidationException("Sort direction must be 'asc' or 'desc'");
        }
    }

    /**
     * Validate required object is not null
     */
    public static void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessValidationException(fieldName + " is required");
        }
    }

    /**
     * Validate ID is positive
     */
    public static void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessValidationException(fieldName + " must be a positive number");
        }
    }

    /**
     * Validate number is positive
     */
    public static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new BusinessValidationException(fieldName + " must be a positive number");
        }
    }

    // ==================== SERVICE POST VALIDATIONS ====================

    /**
     * Validate service post is available for responses
     */
    public static void validateServicePostAvailable(ServicePost servicePost) {
        if (!servicePost.canReceiveResponses()) {
            if (servicePost.isExpired()) {
                throw ServicePostExpiredException.withId(servicePost.getId(), servicePost.getExpiresAt());
            }
            if (servicePost.getResponseCount() >= servicePost.getMaxTechnicians()) {
                throw ServicePostNotAvailableException.maxResponsesReached(servicePost.getId());
            }
            throw ServicePostNotAvailableException.wrongStatus(servicePost.getId(), servicePost.getStatus().toString());
        }
    }

    /**
     * Validate technician hasn't already responded to service post
     */
    public static void validateTechnicianNotResponded(ServicePost servicePost, User technician, 
                                                     boolean alreadyResponded) {
        if (alreadyResponded) {
            throw ServicePostAlreadyRespondedException.withIds(servicePost.getId(), technician.getId());
        }
    }

    /**
     * Validate service post ownership
     */
    public static void validateServicePostOwnership(ServicePost servicePost, User user) {
        if (!servicePost.getCustomer().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: Service post does not belong to user");
        }
    }

    /**
     * Validate service post can be updated by customer
     */
    public static void validateServicePostCanBeUpdated(ServicePost servicePost) {
        if (servicePost.getStatus() == com.fix4home.fix4home.model.enums.ServicePostStatus.COMPLETED ||
            servicePost.getStatus() == com.fix4home.fix4home.model.enums.ServicePostStatus.CANCELLED ||
            servicePost.getStatus() == com.fix4home.fix4home.model.enums.ServicePostStatus.EXPIRED) {
            throw new BusinessValidationException("Cannot update service post in " + servicePost.getStatus() + " status");
        }
    }

    /**
     * Validate service post response can be selected
     */
    public static void validateResponseCanBeSelected(ServicePost servicePost, ServicePostResponse response) {
        // Check if post belongs to current customer
        validateServicePostOwnership(servicePost, response.getServicePost().getCustomer());
        
        // Check if post is in correct status
        if (servicePost.getStatus() != com.fix4home.fix4home.model.enums.ServicePostStatus.POSTED &&
            servicePost.getStatus() != com.fix4home.fix4home.model.enums.ServicePostStatus.RESPONSES_RECEIVED) {
            throw new BusinessValidationException("Cannot select response for service post in " + servicePost.getStatus() + " status");
        }
    }

    /**
     * Validate service post can be cancelled
     */
    public static void validateServicePostCanBeCancelled(ServicePost servicePost) {
        if (servicePost.getStatus() == com.fix4home.fix4home.model.enums.ServicePostStatus.COMPLETED ||
            servicePost.getStatus() == com.fix4home.fix4home.model.enums.ServicePostStatus.CANCELLED ||
            servicePost.getStatus() == com.fix4home.fix4home.model.enums.ServicePostStatus.IN_PROGRESS) {
            throw new BusinessValidationException("Cannot cancel service post in " + servicePost.getStatus() + " status");
        }
    }
} 