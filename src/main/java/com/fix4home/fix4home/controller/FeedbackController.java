package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.feedback.*;
import com.fix4home.fix4home.service.FeedbackService;
import com.fix4home.fix4home.security.SecurityConstants;
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

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedback Controller", description = "Quản lý đánh giá và phản hồi")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ====== PUBLIC ENDPOINTS ======

    @GetMapping("/public")
    @Operation(summary = "Xem đánh giá công khai", description = "Xem danh sách đánh giá công khai")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getPublicFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Đánh giá tối thiểu") @RequestParam(required = false) Integer minRating) {
        Page<FeedbackDTO> feedbacks = feedbackService.getPublicFeedbacks(page, size, minRating);
        return ResponseEntity.ok(ApiResponse.success("Danh sách đánh giá công khai", feedbacks));
    }

    // ====== CUSTOMER ENDPOINTS ======

    @PostMapping("/create")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Tạo đánh giá", description = "Khách hàng tạo đánh giá cho dịch vụ đã hoàn thành")
    public ResponseEntity<ApiResponse<FeedbackDTO>> createFeedback(
            @Valid @RequestBody CreateFeedbackRequest request) {
        Long customerId = getCurrentUserId();
        FeedbackDTO feedback = feedbackService.createFeedback(request, customerId);
        return ResponseEntity.ok(ApiResponse.success("Tạo đánh giá thành công", feedback));
    }

    @GetMapping("/my")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Đánh giá của tôi", description = "Xem danh sách đánh giá đã tạo")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getMyFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        Long customerId = getCurrentUserId();
        Page<FeedbackDTO> feedbacks = feedbackService.getCustomerFeedbacks(customerId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Danh sách đánh giá của bạn", feedbacks));
    }

    @GetMapping("/my/{feedbackId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Chi tiết đánh giá", description = "Xem chi tiết đánh giá đã tạo")
    public ResponseEntity<ApiResponse<FeedbackDTO>> getMyFeedbackDetails(
            @PathVariable Long feedbackId) {
        Long customerId = getCurrentUserId();
        FeedbackDTO feedback = feedbackService.getFeedbackDetails(feedbackId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Chi tiết đánh giá", feedback));
    }

    // ====== TECHNICIAN ENDPOINTS ======

    @GetMapping("/technician/my")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Đánh giá về tôi", description = "Thợ xem đánh giá về mình")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getMyReceivedFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        Long technicianId = getCurrentUserId();
        Page<FeedbackDTO> feedbacks = feedbackService.getTechnicianFeedbacks(technicianId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá về bạn", feedbacks));
    }

    @GetMapping("/technician/my/unreplied")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Đánh giá chưa phản hồi", description = "Thợ xem đánh giá chưa phản hồi")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getMyUnrepliedFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
        Long technicianId = getCurrentUserId();
        Page<FeedbackDTO> feedbacks = feedbackService.getTechnicianUnrepliedFeedbacks(technicianId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá chưa phản hồi", feedbacks));
    }

    @PutMapping("/{feedbackId}/reply")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Phản hồi đánh giá", description = "Thợ phản hồi đánh giá của khách hàng")
    public ResponseEntity<ApiResponse<FeedbackDTO>> replyToFeedback(
            @PathVariable Long feedbackId,
            @Valid @RequestBody ReplyFeedbackRequest request) {
        Long technicianId = getCurrentUserId();
        FeedbackDTO feedback = feedbackService.replyToFeedback(feedbackId, request, technicianId);
        return ResponseEntity.ok(ApiResponse.success("Phản hồi đánh giá thành công", feedback));
    }

    @GetMapping("/technician/my/stats")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Thống kê đánh giá", description = "Thợ xem thống kê đánh giá về mình")
    public ResponseEntity<ApiResponse<FeedbackStatsDTO>> getMyFeedbackStats() {
        Long technicianId = getCurrentUserId();
        FeedbackStatsDTO stats = feedbackService.getTechnicianFeedbackStats(technicianId);
        return ResponseEntity.ok(ApiResponse.success("Thống kê đánh giá của bạn", stats));
    }

    // ====== ADMIN ENDPOINTS ======

    @GetMapping("/admin/all")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "[ADMIN] Xem tất cả đánh giá", description = "Admin xem tất cả đánh giá trong hệ thống")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getAllFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        Page<FeedbackDTO> feedbacks = feedbackService.getAllFeedbacks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Tất cả đánh giá trong hệ thống", feedbacks));
    }

    @GetMapping("/admin/rating/{rating}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "[ADMIN] Đánh giá theo điểm", description = "Admin xem đánh giá theo điểm cụ thể")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getFeedbacksByRating(
            @PathVariable Integer rating,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
        Page<FeedbackDTO> feedbacks = feedbackService.getFeedbacksByRating(rating, page, size);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá " + rating + " sao", feedbacks));
    }

    @GetMapping("/admin/unreplied")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "[ADMIN] Đánh giá chưa phản hồi", description = "Admin xem đánh giá chưa được phản hồi")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getUnrepliedFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
        Page<FeedbackDTO> feedbacks = feedbackService.getUnrepliedFeedbacks(page, size);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá chưa phản hồi", feedbacks));
    }

    @GetMapping("/admin/search")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "[ADMIN] Tìm kiếm đánh giá", description = "Admin tìm kiếm đánh giá theo từ khóa")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> searchFeedbacks(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
        Page<FeedbackDTO> feedbacks = feedbackService.searchFeedbacks(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Kết quả tìm kiếm đánh giá", feedbacks));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "[ADMIN] Thống kê hệ thống", description = "Admin xem thống kê đánh giá toàn hệ thống")
    public ResponseEntity<ApiResponse<FeedbackStatsDTO>> getSystemFeedbackStats() {
        FeedbackStatsDTO stats = feedbackService.getSystemFeedbackStats();
        return ResponseEntity.ok(ApiResponse.success("Thống kê đánh giá hệ thống", stats));
    }

    // ====== HELPER METHODS ======

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
} 