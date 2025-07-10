package com.fix4home.fix4home.model.dto.complaint;

import com.fix4home.fix4home.model.enums.ComplaintStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveComplaintRequest {
    
    @NotNull(message = "Complaint status is required")
    private ComplaintStatus status;
    
    @NotBlank(message = "Admin response is required")
    @Size(min = 10, max = 2000, message = "Admin response must be between 10 and 2000 characters")
    private String adminResponse;
    
    // Custom validation method
    @AssertTrue(message = "Status must be either RESOLVED or REJECTED")
    public boolean isValidStatus() {
        return status == ComplaintStatus.RESOLVED || status == ComplaintStatus.REJECTED;
    }
} 