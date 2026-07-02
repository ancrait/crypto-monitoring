package com.sorokaandriy.telegram_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAlertRequest {
    private Long chatId;
    private String symbol;
    private BigDecimal targetPrice;
    private String direction;
}
