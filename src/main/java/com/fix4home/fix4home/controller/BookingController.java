package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.booking.*;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.enums.BookingStatus;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Controller", description = "APIs for managing bookings/orders")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Create new booking", 
               description = "Customer creates a new booking/order")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        log.info("Creating booking with title: {}", request.getTitle());
        
        BookingDTO booking = bookingService.createBooking(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get list of bookings", 
               description = "Customer retrieves their bookings with optional status filter and pagination")
    public ResponseEntity<ApiResponse<BookingListResponseDTO>> getBookings(
            @Parameter(description = "Filter by status (PENDING, COMPLETED, CANCELLED)") 
            @RequestParam(required = false) BookingStatus status,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (limit)") 
            @RequestParam(required = false) Integer limit) {
        log.info("Fetching bookings - status: {}, page: {}, limit: {}", status, page, limit);
        
        BookingListResponseDTO bookings = bookingService.getBookings(status, page, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get booking details", 
               description = "Get detailed information about a specific booking")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        log.info("Fetching booking details for ID: {}", id);
        
        BookingDTO booking = bookingService.getBookingById(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Booking details retrieved successfully", booking));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Update booking", 
               description = "Customer updates booking details (all fields optional)")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request) {
        log.info("Updating booking with ID: {}", id);
        
        BookingDTO booking = bookingService.updateBooking(id, request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Booking updated successfully", booking));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Cancel booking", 
               description = "Customer cancels a booking")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        log.info("Canceling booking with ID: {}", id);
        
        BookingDTO booking = bookingService.cancelBooking(id);

        return ResponseEntity.ok(
                ApiResponse.success("Booking cancelled successfully", booking));
    }

    // ==================== ADMIN APIS ====================

    @GetMapping("/admin")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Admin - Get all bookings",
               description = "Admin retrieves all bookings with optional status filter and pagination")
    public ResponseEntity<ApiResponse<BookingListResponseDTO>> getAllBookingsForAdmin(
            @Parameter(description = "Filter by status (PENDING, COMPLETED, CANCELLED)")
            @RequestParam(required = false) BookingStatus status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (limit)")
            @RequestParam(required = false) Integer limit) {
        log.info("Admin fetching bookings - status: {}, page: {}, limit: {}", status, page, limit);

        BookingListResponseDTO bookings = bookingService.getAllBookings(status, page, limit);

        return ResponseEntity.ok(
                ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Admin - Get booking details",
               description = "Admin gets detailed information about a specific booking")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingByIdForAdmin(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        log.info("Admin fetching booking details for ID: {}", id);

        BookingDTO booking = bookingService.getBookingByIdForAdmin(id);

        return ResponseEntity.ok(
                ApiResponse.success("Booking details retrieved successfully", booking));
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Admin - Update booking status",
               description = "Admin updates the status of a booking")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBookingStatusByAdmin(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        log.info("Admin updating booking status for ID: {} to {}", id, request.getStatus());

        BookingDTO booking = bookingService.updateBookingStatus(id, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success("Booking status updated successfully", booking));
    }
}

