package com.sorokaandriy.price_alert.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceUpdate {
    private String symbol;
    private BigDecimal priceUsd;
    private String source;
    private Instant timestamp;
}
