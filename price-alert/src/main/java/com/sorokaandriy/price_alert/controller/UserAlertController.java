package com.sorokaandriy.price_alert.controller;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.service.UserAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alerts")
public class UserAlertController {

    private final UserAlertService service;

    @GetMapping("/{chatId}/{symbol}")
    public ResponseEntity<UserAlertResponse> getUserAlert(
            @PathVariable Long chatId,
            @PathVariable String symbol
    ){
        return ResponseEntity.ok(service.getUserAlert(chatId,symbol));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<List<UserAlertResponse>> getUserAlerts(
            @PathVariable Long chatId
    ){
        return ResponseEntity.ok(service.getUserAlerts(chatId));
    }

    @PutMapping("/{chatId}/{symbol}")
    public ResponseEntity<Void> changeEnabled(
            @PathVariable Long chatId,
            @PathVariable String symbol
    ){
        service.changeEnabled(chatId,symbol);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserAlertResponse> createUserAlert(
            @Valid @RequestBody UserAlertRequest request){
        return ResponseEntity.ok(service.createUserAlert(request));
    }

    @PutMapping("/{chatId}/{symbol}")
    public ResponseEntity<UserAlertResponse> updateUserAlert(
            @Valid @RequestBody UserAlertRequest request,
            @PathVariable Long chatId,
            @PathVariable String symbol) {
        return ResponseEntity.ok(service.updateUserAlert(request, chatId, symbol));
    }

    @DeleteMapping("/{chatId}/{symbol}")
    public ResponseEntity<Void> deleteUserAlert(
            @PathVariable Long chatId,
            @PathVariable String symbol
    ){
        service.deleteUserAlert(chatId,symbol);
        return ResponseEntity.noContent().build();
    }




}
