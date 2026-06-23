package com.sorokaandriy.price_alert.controller;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.service.UserAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alerts")
public class UserAlertController {

    private final UserAlertService service;


    @PostMapping
    public ResponseEntity<UserAlertResponse> createUserAlert(
            @Valid @RequestBody UserAlertRequest request){
        return ResponseEntity.ok(service.createUserAlert(request));
    }

    @PutMapping("/{chatId}")
    public ResponseEntity<UserAlertResponse> updateUserAlert(
            @Valid @RequestBody UserAlertRequest request,
            @PathVariable Long chatId){
        return ResponseEntity.ok(service.updateUserAlert(request,chatId));
    }




}
