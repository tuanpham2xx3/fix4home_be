package com.fix4home.fix4home.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatsDTO {
    
    // Overall statistics
    private long totalPayments;
    private long pendingPayments;
    private long paidPayments;
    private long failedPayments;
    
    // Revenue statistics
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal thisWeekRevenue;
    private BigDecimal thisMonthRevenue;
    
    // Payment method breakdown
    private Map<String, Long> paymentsByMethod;
    private Map<String, BigDecimal> revenueByMethod;
    
    // Performance metrics
    private double averagePaymentAmount;
    private double successRate;
    
    // Time-based statistics
    private Map<String, BigDecimal> dailyRevenue;
    private Map<String, BigDecimal> monthlyRevenue;
} 