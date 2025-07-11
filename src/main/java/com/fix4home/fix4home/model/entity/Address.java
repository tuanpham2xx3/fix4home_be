package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(name = "recipient_name", length = 100)
    private String recipientName;
    
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;
    
    @Column(name = "address_line")
    private String addressLine;
    
    @Column(length = 100)
    private String ward;
    
    @Column(length = 100)
    private String district;
    
    @Column(length = 100)
    private String city;
    
    // Vietnam Administrative API integration fields
    @Column(name = "province_code", length = 10)
    private String provinceCode;
    
    @Column(name = "ward_code", length = 10)
    private String wardCode;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;
} 