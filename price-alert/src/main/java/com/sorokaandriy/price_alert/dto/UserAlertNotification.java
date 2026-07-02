package com.sorokaandriy.price_alert.dto;


import com.sorokaandriy.price_alert.entity.enumeration.Direction;
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
public class UserAlertNotification {
    private Long chatId;
    private String symbol;
    private BigDecimal updatedPrice;
    private BigDecimal targetPrice;
    private Direction direction;
    private Instant updatedAt;

}
