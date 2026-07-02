package com.sorokaandriy.price_alert.entity;

import com.sorokaandriy.price_alert.entity.enumeration.Direction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_alerts")
public class UserAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long chatId;
    @Column(nullable = false, length = 10)
    private String symbol;
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal targetPrice;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;
    @Column(nullable = false)
    private Boolean enabled = true;
    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();




}
