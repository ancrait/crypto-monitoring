package com.sorokaandriy.price_alert.dto;

import com.sorokaandriy.price_alert.entity.enumeration.Direction;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAlertRequest {

    @NotNull
    private Long chatId;

    @NotBlank
    @Size(min = 2, max = 10)
    private String symbol;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 8)
    private BigDecimal targetPrice;

    @NotNull
    private Direction direction;



}
