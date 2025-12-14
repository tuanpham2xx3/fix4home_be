package com.fix4home.fix4home.model.dto.booking;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequest {
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String address;
    
    private LocalDateTime date;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Size(max = 20, message = "Ward code must not exceed 20 characters")
    private String wardCode;
    
    private Boolean needsSurvey;
}

