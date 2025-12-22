package com.fix4home.fix4home.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event fired when a complaint is created for a service request.
 */
@Data
@AllArgsConstructor
public class ComplaintCreatedEvent {

    private Long complaintId;
    private Long serviceRequestId;
    private Long complainantId;
    private Long accusedId;
}


