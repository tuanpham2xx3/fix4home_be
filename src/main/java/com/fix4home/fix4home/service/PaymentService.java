package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.payment.*;
import com.fix4home.fix4home.model.entity.Payment;
import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.enums.PaymentMethod;
import com.fix4home.fix4home.model.enums.PaymentStatus;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.repository.PaymentRepository;
import com.fix4home.fix4home.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService extends BaseService {

    private final PaymentRepository paymentRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    @Transactional
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        logBusinessOperation("CREATE_PAYMENT", "requestId=" + request.getServiceRequestId());
        requireRole(Role.CUSTOMER);

        validateRequired(request, "request");
        validateRequired(request.getServiceRequestId(), "serviceRequestId");
        validateRequired(request.getAmount(), "amount");
        validateRequired(request.getMethod(), "method");

        ServiceRequest serviceRequest = findServiceRequestById(request.getServiceRequestId());

        if (serviceRequest.getStatus() != ServiceRequestStatus.DONE) {
            throw new BusinessValidationException("Can only create payment for completed service requests");
        }

        if (paymentRepository.existsByServiceRequestId(request.getServiceRequestId())) {
            throw new BusinessValidationException("Payment already exists for this service request");
        }

        Payment payment = Payment.builder()
                .serviceRequest(serviceRequest)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return convertToDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getCustomerPayments(Long customerId, int page, int size, String sortBy, String sortDir, PaymentStatus status) {
        logBusinessOperation("GET_CUSTOMER_PAYMENTS", "customerId=" + customerId, "status=" + status);
        requireRole(Role.CUSTOMER);

        validatePositiveId(customerId, "customerId");
        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Payment> paymentPage = paymentRepository.findByCustomerId(customerId, pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getTechnicianPayments(Long technicianId, int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_TECHNICIAN_PAYMENTS", "technicianId=" + technicianId);
        requireRole(Role.TECHNICIAN);

        validatePositiveId(technicianId, "technicianId");
        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Payment> paymentPage = paymentRepository.findByTechnicianId(technicianId, pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPendingPayments(Long customerId) {
        logBusinessOperation("GET_PENDING_PAYMENTS", "customerId=" + customerId);
        requireRole(Role.CUSTOMER);

        validatePositiveId(customerId, "customerId");

        List<Payment> payments = paymentRepository.findByCustomerIdAndStatus(customerId, PaymentStatus.PENDING);
        return payments.stream().map(this::convertToDTO).toList();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getCustomerPaymentStats(Long customerId) {
        logBusinessOperation("GET_CUSTOMER_PAYMENT_STATS", "customerId=" + customerId);
        requireRole(Role.CUSTOMER);

        validatePositiveId(customerId, "customerId");

        long totalPayments = paymentRepository.countByCustomerId(customerId);
        BigDecimal totalSpent = paymentRepository.calculateCustomerTotalSpending(customerId);

        return PaymentStatsDTO.builder()
                .totalPayments(totalPayments)
                .totalRevenue(totalSpent)
                .averagePaymentAmount(totalPayments > 0 ? totalSpent.divide(BigDecimal.valueOf(totalPayments), 2, RoundingMode.HALF_UP).doubleValue() : 0)
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getTechnicianPaymentStats(Long technicianId) {
        logBusinessOperation("GET_TECHNICIAN_PAYMENT_STATS", "technicianId=" + technicianId);
        requireRole(Role.TECHNICIAN);

        validatePositiveId(technicianId, "technicianId");

        BigDecimal totalEarnings = paymentRepository.calculateTechnicianEarnings(technicianId);

        return PaymentStatsDTO.builder()
                .totalRevenue(totalEarnings)
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getSystemPaymentStats() {
        logBusinessOperation("GET_SYSTEM_PAYMENT_STATS");
        requireRole(Role.ADMIN);

        long totalPayments = paymentRepository.count();
        long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
        long paidPayments = paymentRepository.countByStatus(PaymentStatus.PAID);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenue();

        return PaymentStatsDTO.builder()
                .totalPayments(totalPayments)
                .pendingPayments(pendingPayments)
                .paidPayments(paidPayments)
                .failedPayments(failedPayments)
                .totalRevenue(totalRevenue)
                .averagePaymentAmount(totalPayments > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalPayments), 2, RoundingMode.HALF_UP).doubleValue() : 0)
                .successRate(totalPayments > 0 ? (double) paidPayments / totalPayments * 100 : 0)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getAllPayments(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_PAYMENTS");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getFailedPayments(int page, int size) {
        logBusinessOperation("GET_FAILED_PAYMENTS");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> paymentPage = paymentRepository.findByStatus(PaymentStatus.FAILED, pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getAvailablePaymentMethods() {
        logBusinessOperation("GET_AVAILABLE_PAYMENT_METHODS");

        List<PaymentMethodDTO> methods = new ArrayList<>();

        methods.add(PaymentMethodDTO.builder()
                .method(PaymentMethod.CASH)
                .displayName("Tiền mặt")
                .description("Thanh toán bằng tiền mặt khi hoàn thành dịch vụ")
                .enabled(true)
                .icon("cash")
                .processingFee(0.0)
                .requiresAdditionalInfo(false)
                .build());

        methods.add(PaymentMethodDTO.builder()
                .method(PaymentMethod.CREDIT_CARD)
                .displayName("Thẻ tín dụng")
                .description("Thanh toán bằng thẻ tín dụng/ghi nợ")
                .enabled(true)
                .icon("credit-card")
                .processingFee(2.5)
                .requiresAdditionalInfo(true)
                .requiresCard(true)
                .supportedCardTypes(Arrays.asList("VISA", "MASTERCARD", "JCB"))
                .build());

        methods.add(PaymentMethodDTO.builder()
                .method(PaymentMethod.BANK_TRANSFER)
                .displayName("Chuyển khoản")
                .description("Chuyển khoản ngân hàng")
                .enabled(true)
                .icon("bank")
                .processingFee(0.0)
                .requiresAdditionalInfo(true)
                .requiresBankInfo(true)
                .supportedBanks(Arrays.asList("Vietcombank", "Techcombank", "Vietinbank", "BIDV"))
                .build());

        methods.add(PaymentMethodDTO.builder()
                .method(PaymentMethod.VNPAY)
                .displayName("VNPay")
                .description("Thanh toán qua cổng VNPay")
                .enabled(true)
                .icon("vnpay")
                .processingFee(1.0)
                .requiresAdditionalInfo(false)
                .isGateway(true)
                .supportsInstantPayment(true)
                .build());

        return methods;
    }

    // ==================== HELPER METHODS ====================

    private ServiceRequest findServiceRequestById(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
    }

    private PaymentDTO convertToDTO(Payment payment) {
        String timeAgo = calculateTimeAgo(payment.getCreatedAt());
        
        PaymentDTO dto = PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .paymentTime(payment.getPaymentTime())
                .serviceRequestId(payment.getServiceRequest().getId())
                .serviceRequestDescription(payment.getServiceRequest().getDescription())
                .serviceName(payment.getServiceRequest().getService().getName())
                .customerId(payment.getServiceRequest().getCustomer().getId())
                .customerName(payment.getServiceRequest().getCustomer().getUsername())
                .customerEmail(payment.getServiceRequest().getCustomer().getEmail())
                .timeAgo(timeAgo)
                .formattedAmount(formatAmount(payment.getAmount()))
                .statusDisplay(getStatusDisplay(payment.getStatus()))
                .methodDisplay(getMethodDisplay(payment.getMethod()))
                .canCancel(payment.getStatus() == PaymentStatus.PENDING)
                .canRefund(payment.getStatus() == PaymentStatus.PAID)
                .build();

        if (payment.getServiceRequest().getTechnician() != null) {
            dto.setTechnicianId(payment.getServiceRequest().getTechnician().getId());
            dto.setTechnicianName(payment.getServiceRequest().getTechnician().getUsername());
            dto.setTechnicianEmail(payment.getServiceRequest().getTechnician().getEmail());
        }

        return dto;
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        
        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        
        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    private String getStatusDisplay(PaymentStatus status) {
        switch (status) {
            case PENDING: return "Chờ thanh toán";
            case PAID: return "Đã thanh toán";
            case FAILED: return "Thất bại";
            default: return status.toString();
        }
    }

    private String getMethodDisplay(PaymentMethod method) {
        switch (method) {
            case CASH: return "Tiền mặt";
            case CREDIT_CARD: return "Thẻ tín dụng";
            case BANK_TRANSFER: return "Chuyển khoản";
            case VNPAY: return "VNPay";
            default: return method.toString();
        }
    }
} 