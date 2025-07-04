package com.fix4home.fix4home.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkOperationResultDTO {
    
    private BulkOperationRequest.BulkOperationType operationType;
    private int totalTargets;
    private int successCount;
    private int failureCount;
    private List<OperationError> errors;
    private LocalDateTime executedAt;
    private String executedBy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperationError {
        private Long targetId;
        private String errorMessage;
        private String errorCode;
    }
} 