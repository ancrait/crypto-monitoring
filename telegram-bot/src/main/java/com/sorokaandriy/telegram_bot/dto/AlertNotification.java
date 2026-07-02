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
public class AlertNotification {
    private Long chatId;
    private String symbol;
    private BigDecimal updatedPrice;
    private BigDecimal targetPrice;
    private String direction;
    private Instant updatedAt;
}
