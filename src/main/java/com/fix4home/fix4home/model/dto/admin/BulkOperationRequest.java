package com.fix4home.fix4home.model.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkOperationRequest {
    
    @NotEmpty(message = "Target IDs cannot be empty")
    private List<Long> targetIds;
    
    @NotNull(message = "Operation type is required")
    private BulkOperationType operationType;
    
    private String reason;
    private Object operationData; // Additional data for specific operations
    
    public enum BulkOperationType {
        ACTIVATE_USERS,
        DEACTIVATE_USERS,
        DELETE_USERS,
        APPROVE_TECHNICIANS,
        REJECT_TECHNICIANS,
        ACTIVATE_SERVICES,
        DEACTIVATE_SERVICES,
        DELETE_SERVICES,
        CANCEL_REQUESTS,
        ASSIGN_REQUESTS
    }
} 