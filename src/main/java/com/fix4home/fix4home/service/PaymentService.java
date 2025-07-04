package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.model.dto.payment.*;
import com.fix4home.fix4home.model.entity.Payment;
import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.enums.PaymentMethod;
import com.fix4home.fix4home.model.enums.PaymentStatus;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    @Transactional
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for service request ID: {}", request.getServiceRequestId());

        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
                .orElseThrow(() -> new BadRequestException("Service request not found"));

        if (serviceRequest.getStatus() != ServiceRequestStatus.DONE) {
            throw new BadRequestException("Can only create payment for completed service requests");
        }

        if (paymentRepository.existsByServiceRequestId(request.getServiceRequestId())) {
            throw new BadRequestException("Payment already exists for this service request");
        }

        Payment payment = Payment.builder()
                .serviceRequest(serviceRequest)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully with ID: {}", savedPayment.getId());

        return convertToDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getCustomerPayments(Long customerId, int page, int size, String sortBy, String sortDir, PaymentStatus status) {
        log.info("Fetching payments for customer ID: {} - page: {}, size: {}, status: {}", customerId, page, size, status);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Payment> paymentPage = paymentRepository.findByCustomerId(customerId, pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getTechnicianPayments(Long technicianId, int page, int size, String sortBy, String sortDir) {
        log.info("Fetching payments for technician ID: {} - page: {}, size: {}", technicianId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Payment> paymentPage = paymentRepository.findByTechnicianId(technicianId, pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPendingPayments(Long customerId) {
        log.info("Fetching pending payments for customer ID: {}", customerId);
        List<Payment> payments = paymentRepository.findByCustomerIdAndStatus(customerId, PaymentStatus.PENDING);
        return payments.stream().map(this::convertToDTO).toList();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getCustomerPaymentStats(Long customerId) {
        log.info("Generating payment statistics for customer ID: {}", customerId);

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
        log.info("Generating payment statistics for technician ID: {}", technicianId);

        BigDecimal totalEarnings = paymentRepository.calculateTechnicianEarnings(technicianId);

        return PaymentStatsDTO.builder()
                .totalRevenue(totalEarnings)
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getSystemPaymentStats() {
        log.info("Generating system-wide payment statistics");

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
        log.info("Admin fetching all payments - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getFailedPayments(int page, int size) {
        log.info("Admin fetching failed payments - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> paymentPage = paymentRepository.findByStatus(PaymentStatus.FAILED, pageable);
        
        return paymentPage.map(this::convertToDTO);
    }

    public List<PaymentMethodDTO> getAvailablePaymentMethods() {
        log.info("Fetching available payment methods");

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