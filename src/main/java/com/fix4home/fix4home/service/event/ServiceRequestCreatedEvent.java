package com.fix4home.fix4home.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event fired when a new service request is created by customer.
 */
@Data
@AllArgsConstructor
public class ServiceRequestCreatedEvent {

    private Long serviceRequestId;
    private Long customerId;
    private Long technicianId;
    private String serviceName;
    private String addressSummary;
}


