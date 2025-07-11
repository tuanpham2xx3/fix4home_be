package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.customer.*;
import com.fix4home.fix4home.service.CustomerService;
import com.fix4home.fix4home.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customer profiles and addresses")
public class CustomerController {

    private final CustomerService customerService;

    // ==================== PROFILE MANAGEMENT ====================

    @GetMapping("/profile")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my profile", description = "Get current customer's profile information")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> getMyProfile() {
        log.info("GET /api/v1/customers/profile - Get current customer profile");
        CustomerProfileDTO profile = customerService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Update my profile", description = "Update current customer's profile information")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> updateMyProfile(
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        log.info("PUT /api/v1/customers/profile - Update current customer profile");
        CustomerProfileDTO updatedProfile = customerService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @GetMapping("/{userId}/profile")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get customer profile by ID", description = "Get specific customer's profile - Admin only")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> getCustomerProfile(
            @Parameter(description = "Customer User ID") @PathVariable Long userId) {
        log.info("GET /api/v1/customers/{}/profile - Admin getting customer profile", userId);
        CustomerProfileDTO profile = customerService.getCustomerProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Customer profile retrieved successfully", profile));
    }

    @PutMapping("/{userId}/profile")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Update customer profile by ID", description = "Update specific customer's profile - Admin only")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> updateCustomerProfile(
            @Parameter(description = "Customer User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        log.info("PUT /api/v1/customers/{}/profile - Admin updating customer profile", userId);
        CustomerProfileDTO updatedProfile = customerService.updateCustomerProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Customer profile updated successfully", updatedProfile));
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all customers", description = "Get all customer profiles - Admin only")
    public ResponseEntity<ApiResponse<List<CustomerProfileDTO>>> getAllCustomers() {
        log.info("GET /api/v1/customers - Admin getting all customers");
        List<CustomerProfileDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success("All customers retrieved successfully", customers));
    }

    @GetMapping("/paginated")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get customers with pagination", description = "Get customers with pagination - Admin only")
    public ResponseEntity<ApiResponse<Page<CustomerProfileDTO>>> getCustomersWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "username") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/v1/customers/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Page<CustomerProfileDTO> customers = customerService.getAllCustomersWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    // ==================== ADDRESS MANAGEMENT ====================

    @GetMapping("/addresses")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my addresses", description = "Get current customer's addresses")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getMyAddresses() {
        log.info("GET /api/v1/customers/addresses - Get current customer addresses");
        List<AddressDTO> addresses = customerService.getMyAddresses();
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    @PostMapping("/addresses")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Create new address", description = "Create a new address for current customer")
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(
            @Valid @RequestBody CreateAddressRequest request) {
        log.info("POST /api/v1/customers/addresses - Create new address for current customer");
        AddressDTO createdAddress = customerService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", createdAddress));
    }

    @GetMapping("/addresses/{addressId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get address by ID", description = "Get specific address details")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddressById(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        log.info("GET /api/v1/customers/addresses/{} - Get address by ID", addressId);
        AddressDTO address = customerService.getAddressById(addressId);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
    }

    @PutMapping("/addresses/{addressId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Update address", description = "Update customer's own address")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        log.info("PUT /api/v1/customers/addresses/{} - Update address", addressId);
        AddressDTO updatedAddress = customerService.updateAddress(addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updatedAddress));
    }

    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Delete address", description = "Delete customer's own address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        log.info("DELETE /api/v1/customers/addresses/{} - Delete address", addressId);
        customerService.deleteAddress(addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    // ==================== ADDRESS SEARCH BY LOCATION ====================

    @GetMapping("/addresses/search/by-province")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Search addresses by province", description = "Search customer's addresses by province code")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> searchAddressesByProvince(
            @Parameter(description = "Province code") @RequestParam String provinceCode) {
        log.info("GET /api/v1/customers/addresses/search/by-province - Search addresses by province: {}", provinceCode);
        List<AddressDTO> addresses = customerService.getAddressesByProvince(provinceCode);
        return ResponseEntity.ok(ApiResponse.success("Addresses found successfully", addresses));
    }

    @GetMapping("/addresses/search/by-ward")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Search addresses by ward", description = "Search customer's addresses by ward code")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> searchAddressesByWard(
            @Parameter(description = "Ward code") @RequestParam String wardCode) {
        log.info("GET /api/v1/customers/addresses/search/by-ward - Search addresses by ward: {}", wardCode);
        List<AddressDTO> addresses = customerService.getAddressesByWard(wardCode);
        return ResponseEntity.ok(ApiResponse.success("Addresses found successfully", addresses));
    }



    // ==================== ADMIN ADDRESS MANAGEMENT ====================

    @GetMapping("/{userId}/addresses")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get customer addresses", description = "Get addresses for specific customer - Admin only")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getCustomerAddresses(
            @Parameter(description = "Customer User ID") @PathVariable Long userId) {
        log.info("GET /api/v1/customers/{}/addresses - Admin getting customer addresses", userId);
        List<AddressDTO> addresses = customerService.getCustomerAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success("Customer addresses retrieved successfully", addresses));
    }

    // ==================== UTILITY ENDPOINTS ====================

    @GetMapping("/health")
    @Operation(summary = "Customer controller health check", description = "Health check for customer management endpoints")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Customer Controller is working", "OK"));
    }
} 