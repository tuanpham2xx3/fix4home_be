package com.fix4home.fix4home.service.event;

import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event fired when a service request status changes (e.g. START_WORK, COMPLETE_WORK).
 */
@Data
@AllArgsConstructor
public class ServiceRequestStatusChangedEvent {

    private Long serviceRequestId;
    private Long customerId;
    private Long technicianId;
    private ServiceRequestStatus oldStatus;
    private ServiceRequestStatus newStatus;
}


