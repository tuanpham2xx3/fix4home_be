package com.fix4home.fix4home.model.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyFeedbackRequest {
    
    @NotBlank(message = "Reply cannot be empty")
    @Size(max = 1000, message = "Reply cannot exceed 1000 characters")
    private String reply;
} 