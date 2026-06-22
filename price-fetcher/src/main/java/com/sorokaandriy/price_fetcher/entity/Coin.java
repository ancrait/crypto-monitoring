package com.sorokaandriy.price_fetcher.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "crypto_prices")
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 10, nullable = false)
    private String symbol;
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal priceUsd;
    @Column(length = 20, nullable = false)
    private String source;
    @Column(nullable = false)
    @Builder.Default
    private Instant timestamp = Instant.now();




}
