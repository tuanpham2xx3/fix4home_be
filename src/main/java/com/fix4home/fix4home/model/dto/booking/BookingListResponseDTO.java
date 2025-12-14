package com.fix4home.fix4home.model.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingListResponseDTO {
    
    private List<BookingDTO> bookings;
    private Long total;
    private Integer page;
    private Integer limit;
}

