package com.sorokaandriy.price_alert.controller;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.service.UserAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alerts")
public class UserAlertController {

    private final UserAlertService service;

    @GetMapping("/{chatId}/{symbol}/{targetPrice}")
    public ResponseEntity<UserAlertResponse> getUserAlert(
            @PathVariable Long chatId,
            @PathVariable String symbol,
            @PathVariable BigDecimal targetPrice
    ){
        return ResponseEntity.ok(service.getUserAlert(chatId,symbol,targetPrice));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<List<UserAlertResponse>> getUserAlerts(
            @PathVariable Long chatId
    ){
        return ResponseEntity.ok(service.getUserAlerts(chatId));
    }

    @PatchMapping("/{chatId}/{symbol}/{targetPrice}/toggle")
    public ResponseEntity<Boolean> changeEnabled(
            @PathVariable Long chatId,
            @PathVariable String symbol,
            @PathVariable BigDecimal targetPrice
    ){
        return ResponseEntity.ok(service.changeEnabled(chatId, symbol, targetPrice));
    }

    @PostMapping
    public ResponseEntity<UserAlertResponse> createUserAlert(
            @Valid @RequestBody UserAlertRequest request){
        return ResponseEntity.ok(service.createUserAlert(request));
    }

    @PutMapping("/{chatId}/{symbol}/{targetPrice}")
    public ResponseEntity<UserAlertResponse> updateUserAlert(
            @Valid @RequestBody UserAlertRequest request,
            @PathVariable Long chatId,
            @PathVariable String symbol,
            @PathVariable BigDecimal targetPrice)
    {
        return ResponseEntity.ok(service.updateUserAlert(request, chatId, symbol,targetPrice));
    }

    @DeleteMapping("/{chatId}/{symbol}/{targetPrice}")
    public ResponseEntity<Void> deleteUserAlert(
            @PathVariable Long chatId,
            @PathVariable String symbol,
            @PathVariable BigDecimal targetPrice
    ){
        service.deleteUserAlert(chatId,symbol,targetPrice);
        return ResponseEntity.noContent().build();
    }


}
