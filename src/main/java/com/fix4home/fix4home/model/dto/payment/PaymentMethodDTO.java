package com.fix4home.fix4home.model.dto.payment;

import com.fix4home.fix4home.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodDTO {
    
    private PaymentMethod method;
    private String displayName;
    private String description;
    private boolean enabled;
    private String icon;
    private double processingFee;
    private List<String> supportedCurrencies;
    private boolean requiresAdditionalInfo;
    
    // For credit card
    private boolean requiresCard;
    private List<String> supportedCardTypes;
    
    // For bank transfer
    private boolean requiresBankInfo;
    private List<String> supportedBanks;
    
    // For VNPay
    private boolean isGateway;
    private String gatewayUrl;
    private boolean supportsInstantPayment;
} 