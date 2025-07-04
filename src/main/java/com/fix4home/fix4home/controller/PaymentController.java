package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.payment.*;
import com.fix4home.fix4home.model.enums.PaymentStatus;
import com.fix4home.fix4home.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Quản lý thanh toán")
public class PaymentController {

    private final PaymentService paymentService;

    // ====== PUBLIC ENDPOINTS ======

    @GetMapping("/methods")
    @Operation(summary = "Lấy danh sách phương thức thanh toán", description = "Lấy danh sách các phương thức thanh toán có sẵn")
    public ResponseEntity<ApiResponse<List<PaymentMethodDTO>>> getAvailablePaymentMethods() {
        List<PaymentMethodDTO> methods = paymentService.getAvailablePaymentMethods();
        return ResponseEntity.ok(ApiResponse.success("Danh sách phương thức thanh toán", methods));
    }

    // ====== CUSTOMER ENDPOINTS ======

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Tạo thanh toán", description = "Khách hàng tạo thanh toán cho dịch vụ đã hoàn thành")
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentDTO payment = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo thanh toán thành công", payment));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Lịch sử thanh toán của khách hàng", description = "Xem lịch sử thanh toán của khách hàng")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getMyPayments(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) PaymentStatus status) {
        Long customerId = getCurrentUserId();
        Page<PaymentDTO> payments = paymentService.getCustomerPayments(customerId, page, size, sortBy, sortDir, status);
        return ResponseEntity.ok(ApiResponse.success("Danh sách thanh toán của bạn", payments));
    }

    @GetMapping("/my/pending")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Thanh toán chờ xử lý", description = "Xem danh sách thanh toán chờ xử lý của khách hàng")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getMyPendingPayments() {
        Long customerId = getCurrentUserId();
        List<PaymentDTO> payments = paymentService.getPendingPayments(customerId);
        return ResponseEntity.ok(ApiResponse.success("Danh sách thanh toán chờ xử lý", payments));
    }

    @GetMapping("/my/stats")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Thống kê thanh toán khách hàng", description = "Xem thống kê thanh toán của khách hàng")
    public ResponseEntity<ApiResponse<PaymentStatsDTO>> getMyPaymentStats() {
        Long customerId = getCurrentUserId();
        PaymentStatsDTO stats = paymentService.getCustomerPaymentStats(customerId);
        return ResponseEntity.ok(ApiResponse.success("Thống kê thanh toán của bạn", stats));
    }

    // ====== TECHNICIAN ENDPOINTS ======

    @GetMapping("/technician/my")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Thu nhập của thợ", description = "Xem thu nhập từ các công việc đã hoàn thành")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getMyEarnings(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        Long technicianId = getCurrentUserId();
        Page<PaymentDTO> payments = paymentService.getTechnicianPayments(technicianId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Thu nhập của bạn", payments));
    }

    @GetMapping("/technician/my/stats")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Thống kê thu nhập thợ", description = "Xem thống kê thu nhập của thợ")
    public ResponseEntity<ApiResponse<PaymentStatsDTO>> getMyEarningsStats() {
        Long technicianId = getCurrentUserId();
        PaymentStatsDTO stats = paymentService.getTechnicianPaymentStats(technicianId);
        return ResponseEntity.ok(ApiResponse.success("Thống kê thu nhập của bạn", stats));
    }

    // ====== ADMIN ENDPOINTS ======

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Xem tất cả thanh toán", description = "Admin xem tất cả thanh toán trong hệ thống")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getAllPayments(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        Page<PaymentDTO> payments = paymentService.getAllPayments(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Tất cả thanh toán trong hệ thống", payments));
    }

    @GetMapping("/admin/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Thanh toán thất bại", description = "Admin xem danh sách thanh toán thất bại")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getFailedPayments(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
        Page<PaymentDTO> payments = paymentService.getFailedPayments(page, size);
        return ResponseEntity.ok(ApiResponse.success("Danh sách thanh toán thất bại", payments));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Thống kê thanh toán hệ thống", description = "Admin xem thống kê tổng quan thanh toán")
    public ResponseEntity<ApiResponse<PaymentStatsDTO>> getSystemPaymentStats() {
        PaymentStatsDTO stats = paymentService.getSystemPaymentStats();
        return ResponseEntity.ok(ApiResponse.success("Thống kê thanh toán hệ thống", stats));
    }

    // ====== HELPER METHODS ======

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
} 