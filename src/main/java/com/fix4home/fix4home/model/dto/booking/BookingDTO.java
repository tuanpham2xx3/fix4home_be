package com.fix4home.fix4home.model.dto.booking;

import com.fix4home.fix4home.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    
    private Long id;
    private String title;
    private String address;
    private LocalDateTime date;
    private String notes;
    private String phone;
    private String name;
    private String wardCode;
    private Boolean needsSurvey;
    private BookingStatus status;
    private Long userId; // User ID - populated for admin view
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

