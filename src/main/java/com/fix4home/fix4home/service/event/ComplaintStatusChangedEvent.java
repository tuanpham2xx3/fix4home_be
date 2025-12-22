package com.fix4home.fix4home.service.event;

import com.fix4home.fix4home.model.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event fired when a complaint status changes.
 */
@Data
@AllArgsConstructor
public class ComplaintStatusChangedEvent {

    private Long complaintId;
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
}


