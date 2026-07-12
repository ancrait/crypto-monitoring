package com.sorokaandriy.price_alert.controller;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.entity.enumeration.Direction;
import com.sorokaandriy.price_alert.service.UserAlertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAlertControllerTest {

    @Mock
    private UserAlertService userAlertService;

    @InjectMocks
    private UserAlertController userAlertController;

    @Test
    void shouldReturnUserAlert() {

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        UserAlertResponse response = new UserAlertResponse(12L, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        when(userAlertService.getUserAlert(chatId, symbol, targetPrice))
                .thenReturn(response);

        ResponseEntity<UserAlertResponse> result = userAlertController
                .getUserAlert(chatId, symbol, targetPrice);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(userAlertService).getUserAlert(chatId, symbol, targetPrice);
    }

    @Test
    void shouldReturnUserAlerts() {

        Long chatId = 132117L;

        UserAlertResponse response1 = new UserAlertResponse(12L, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        UserAlertResponse response2 = new UserAlertResponse(12L, "SOL",
                new BigDecimal("20000.00"), Direction.BELOW, true, Instant.now());

        when(userAlertService.getUserAlerts(chatId))
                .thenReturn(List.of(response1, response2));

        ResponseEntity<List<UserAlertResponse>> result = userAlertController
                .getUserAlerts(chatId);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(response1, result.getBody().get(0));
        assertEquals(response2, result.getBody().get(1));
        verify(userAlertService).getUserAlerts(chatId);


    }

    @Test
    void shouldChangedEnabled(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        when(userAlertService.changeEnabled(chatId, symbol, targetPrice))
                .thenReturn(false);

        ResponseEntity<Boolean> result = userAlertController.
                changeEnabled(chatId, symbol, targetPrice);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(false, result.getBody());
        verify(userAlertService).changeEnabled(chatId,symbol, targetPrice);
    }


    @Test
    void shouldUpdateUserAlert(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        UserAlertRequest request = new UserAlertRequest(
                chatId, symbol, targetPrice, Direction.ABOVE
        );

        UserAlertResponse response1 = new UserAlertResponse(12L, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        when(userAlertService.updateUserAlert(request,chatId,symbol,targetPrice))
                .thenReturn(response1);

        ResponseEntity<UserAlertResponse> result =
                userAlertController.updateUserAlert(request,chatId,symbol,targetPrice);

        assertNotNull(result);
        assertEquals(response1, result.getBody());
        assertEquals(200, result.getStatusCode().value());
        verify(userAlertService).updateUserAlert(request,chatId,symbol,targetPrice);


    }


    @Test
    void shouldCreateUserAlert(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        UserAlertRequest request = new UserAlertRequest(
                chatId, symbol, targetPrice, Direction.ABOVE
        );

        UserAlertResponse response1 = new UserAlertResponse(12L, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        when(userAlertService.createUserAlert(request))
                .thenReturn(response1);

        ResponseEntity<UserAlertResponse> result =
                userAlertController.createUserAlert(request);

        assertNotNull(result);
        assertEquals(response1, result.getBody());
        assertEquals(200, result.getStatusCode().value());
        verify(userAlertService).createUserAlert(request);
    }


    @Test
    void shouldDeleteUserAlert(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        ResponseEntity<Void> result = userAlertController
                .deleteUserAlert(chatId, symbol, targetPrice);

        assertNotNull(result);
        assertEquals(204, result.getStatusCode().value());
        verify(userAlertService).deleteUserAlert(chatId, symbol, targetPrice);
    }





}
