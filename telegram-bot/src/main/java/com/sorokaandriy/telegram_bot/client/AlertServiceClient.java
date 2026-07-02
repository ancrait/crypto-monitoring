package com.sorokaandriy.telegram_bot.client;

import com.sorokaandriy.telegram_bot.dto.UserAlertRequest;
import com.sorokaandriy.telegram_bot.dto.UserAlertResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AlertServiceClient {

    private final RestClient restClient;
    private final String alertServiceUrl;

    public AlertServiceClient(@Value("${app.alert-service.url}") String alertServiceUrl) {
        this.alertServiceUrl = alertServiceUrl;
        this.restClient = RestClient.create();
    }

    public List<UserAlertResponse> getUserAlerts(Long chatId) {
        return restClient.get()
                .uri(alertServiceUrl + "/{chatId}", chatId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserAlertResponse>>() {});
    }

    public UserAlertResponse getUserAlert(Long chatId, String symbol, BigDecimal targetPrice){
        return restClient.get()
                .uri(alertServiceUrl + "/{chatId}/{symbol}/{targetPrice}", chatId, symbol, targetPrice)
                .retrieve()
                .body(UserAlertResponse.class);
    }

    public UserAlertResponse createAlert(UserAlertRequest request) {
        return restClient.post()
                .uri(alertServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(UserAlertResponse.class);
    }

    public UserAlertResponse updateUserAlert(UserAlertRequest request,
                                             Long chatId, String symbol, BigDecimal targetPrice){
        return restClient.put()
                .uri(alertServiceUrl + "/{chatId}/{symbol}/{targetPrice}", chatId, symbol, targetPrice)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(UserAlertResponse.class);
    }

    public Boolean changeEnabled(Long chatId, String symbol, BigDecimal targetPrice) {
        return restClient.patch()
                .uri(alertServiceUrl + "/{chatId}/{symbol}/{targetPrice}/toggle", chatId, symbol, targetPrice)
                .retrieve()
                .body(Boolean.class);
    }

    public ResponseEntity<Void> deleteUserAlert(Long chatId, String symbol, BigDecimal targetPrice){
        return restClient.delete()
                .uri(alertServiceUrl + "/{chatId}/{symbol}/{targetPrice}" ,chatId, symbol, targetPrice)
                .retrieve()
                .toBodilessEntity();
    }






}
