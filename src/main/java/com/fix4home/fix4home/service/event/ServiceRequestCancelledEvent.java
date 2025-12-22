package com.fix4home.fix4home.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event fired when a service request is cancelled.
 */
@Data
@AllArgsConstructor
public class ServiceRequestCancelledEvent {

    private Long serviceRequestId;
    private Long customerId;
    private Long technicianId;
    private String cancelReason;
}


