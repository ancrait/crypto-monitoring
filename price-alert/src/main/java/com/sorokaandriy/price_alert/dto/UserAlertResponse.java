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
public class UserAlertResponse {
    private Long id;
    private String symbol;
    private BigDecimal targetPrice;
    private Direction direction;
    private Boolean enabled;
    private Instant createdAt;
}
