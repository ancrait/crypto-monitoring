package com.sorokaandriy.telegram_bot.dto;

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
public class UserAlertResponse {
    private Long id;
    private String symbol;
    private BigDecimal targetPrice;
    private String direction;
    private Boolean enabled;
    private Instant createdAt;
}
