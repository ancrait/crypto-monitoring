package com.sorokaandriy.handler;

import com.sorokaandriy.telegram_bot.bot.CryptoBot;
import com.sorokaandriy.telegram_bot.client.AlertServiceClient;
import com.sorokaandriy.telegram_bot.dto.UserAlertRequest;
import com.sorokaandriy.telegram_bot.dto.UserAlertResponse;
import com.sorokaandriy.telegram_bot.handler.HandlerBotsCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlerBotsCommandTest {

    @Mock
    private AlertServiceClient alertServiceClient;
    @Mock
    private CryptoBot cryptoBot;

    @InjectMocks
    private HandlerBotsCommand handlerBotsCommand;

    private final Long chatId = 132117L;

    @Test
    void shouldReturnUsageWhenNotEnoughArgs() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC");

        verify(cryptoBot).sendMessage(chatId, "Usage: /track BTC 100000 [below]");
        verifyNoInteractions(alertServiceClient);
    }

    @Test
    void shouldReturnErrorWhenUnknownCoin() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track DOGE 100");

        verify(cryptoBot).sendMessage(chatId, "Unknown coin: DOGE. Available: BTC, ETH, SOL");
        verifyNoInteractions(alertServiceClient);
    }

    @Test
    void shouldReturnErrorWhenInvalidPrice() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC abc");

        verify(cryptoBot).sendMessage(chatId, "Invalid price format: abc");
        verifyNoInteractions(alertServiceClient);
    }

    @Test
    void shouldReturnErrorWhenPriceZeroOrNegative() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC 0");

        verify(cryptoBot).sendMessage(chatId, "Price must be greater than 0");
        verifyNoInteractions(alertServiceClient);
    }

    @Test
    void shouldReturnErrorWhenInvalidDirection() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC 100 sideways");

        verify(cryptoBot).sendMessage(chatId, "Direction must be 'above' or 'below', got: sideways");
        verifyNoInteractions(alertServiceClient);
    }

    @Test
    void shouldCreateWithDefaultDirectionAbove() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC 100");

        ArgumentCaptor<UserAlertRequest> captor = ArgumentCaptor.forClass(UserAlertRequest.class);
        verify(alertServiceClient).createAlert(captor.capture());
        UserAlertRequest request = captor.getValue();
        assertEquals(chatId, request.getChatId());
        assertEquals("BTC", request.getSymbol());
        assertEquals(new BigDecimal("100"), request.getTargetPrice());
        assertEquals("ABOVE", request.getDirection());

        verify(cryptoBot).sendMessage(chatId, "BTC — ABOVE $100");
    }

    @Test
    void shouldCreateWithExplicitAboveDirection() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC 100 above");

        ArgumentCaptor<UserAlertRequest> captor = ArgumentCaptor.forClass(UserAlertRequest.class);
        verify(alertServiceClient).createAlert(captor.capture());
        assertEquals("ABOVE", captor.getValue().getDirection());

        verify(cryptoBot).sendMessage(chatId, "BTC — ABOVE $100");
    }

    @Test
    void shouldCreateWithBelowDirection() {
        handlerBotsCommand.handleTrackCommand(chatId, "/track ETH 5000 below");

        ArgumentCaptor<UserAlertRequest> captor = ArgumentCaptor.forClass(UserAlertRequest.class);
        verify(alertServiceClient).createAlert(captor.capture());
        UserAlertRequest request = captor.getValue();
        assertEquals("ETH", request.getSymbol());
        assertEquals(new BigDecimal("5000"), request.getTargetPrice());
        assertEquals("BELOW", request.getDirection());

        verify(cryptoBot).sendMessage(chatId, "ETH — BELOW $5000");
    }

    @Test
    void shouldReturnErrorWhenAlertAlreadyExists() {
        doThrow(new RuntimeException("already exists")).when(alertServiceClient).createAlert(any());

        handlerBotsCommand.handleTrackCommand(chatId, "/track BTC 100");

        verify(cryptoBot).sendMessage(chatId, "Alert already exists: BTC at $100");
    }

    @Test
    void shouldHandleExtraSpacesInCommand() {
        handlerBotsCommand.handleTrackCommand(chatId, "   /track   BTC   100   below   ");

        ArgumentCaptor<UserAlertRequest> captor = ArgumentCaptor.forClass(UserAlertRequest.class);
        verify(alertServiceClient).createAlert(captor.capture());
        assertEquals("BELOW", captor.getValue().getDirection());

        verify(cryptoBot).sendMessage(chatId, "BTC — BELOW $100");
    }

    @Test
    void handleUntrackCommand() {
        handlerBotsCommand.handleUntrackCommand(chatId, "/untrack BTC 10000");

        verify(alertServiceClient).deleteUserAlert(chatId, "BTC", new BigDecimal("10000"));
        verify(cryptoBot).sendMessage(chatId, "BTC removed");
    }

    @Test
    void handleDoesntExistCoinUntrackCommand(){

        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("10000");

//        doThrow(new RuntimeException("already exists")).when(alertServiceClient).createAlert(any());

        doThrow(new RuntimeException("already exist")).when(alertServiceClient).deleteUserAlert(chatId,symbol,targetPrice);
        handlerBotsCommand.handleUntrackCommand(chatId, "/untrack BTC 10000");

        verify(cryptoBot).sendMessage(chatId, "Alert with symbol " + symbol + " and target price " + targetPrice +
                " doesnt exist");
    }

    @Test
    void handleUpdateCommand(){

        handlerBotsCommand.handleUpdateCommand(chatId, "/update BTC 95000 100000 above");

        ArgumentCaptor<UserAlertRequest> captor = ArgumentCaptor.forClass(UserAlertRequest.class);
        verify(alertServiceClient).updateUserAlert(captor.capture(), eq(chatId), eq("BTC"),
                eq(new BigDecimal("95000")));
        UserAlertRequest request = captor.getValue();
        assertEquals("BTC", request.getSymbol());
        assertEquals(new BigDecimal("100000"), request.getTargetPrice());
        assertEquals(chatId, request.getChatId());

        verify(cryptoBot).sendMessage(chatId, request.getSymbol() + " updated: "
                + request.getDirection() + " $" + request.getTargetPrice());
    }

    @Test
    void shouldReturnNotFoundWhenUpdateFails() {
        doThrow(new RuntimeException()).when(alertServiceClient)
                .updateUserAlert(any(), eq(chatId), eq("BTC"), eq(new BigDecimal("95000")));

        handlerBotsCommand.handleUpdateCommand(chatId, "/update BTC 95000 100000 above");

        verify(cryptoBot).sendMessage(chatId, "Alert with symbol BTC and target price 95000" +
                " not found");
    }

    @Test
    void shouldPauseAlert() {
        when(alertServiceClient.changeEnabled(chatId, "BTC", new BigDecimal("100000")))
                .thenReturn(false);

        handlerBotsCommand.handlePauseResumeCommand(chatId, "/pause BTC 100000");

        verify(cryptoBot).sendMessage(chatId, "BTC paused");
    }

    @Test
    void shouldResumeAlert() {
        when(alertServiceClient.changeEnabled(chatId, "BTC", new BigDecimal("100000")))
                .thenReturn(true);

        handlerBotsCommand.handlePauseResumeCommand(chatId, "/resume BTC 100000");

        verify(cryptoBot).sendMessage(chatId, "BTC resumed");
    }

    @Test
    void shouldReturnNotFoundWhenPauseResumeFails() {
        doThrow(new RuntimeException()).when(alertServiceClient)
                .changeEnabled(chatId, "BTC", new BigDecimal("100000"));

        handlerBotsCommand.handlePauseResumeCommand(chatId, "/pause BTC 100000");

        verify(cryptoBot).sendMessage(chatId, "Alert with symbol BTC and target price 100000" +
                " doesnt exist");
    }

    @Test
    void shouldReturnNoAlertsWhenListIsEmpty() {
        when(alertServiceClient.getUserAlerts(chatId)).thenReturn(List.of());

        handlerBotsCommand.handleListCommand(chatId);

        verify(cryptoBot).sendMessage(chatId, "You have no active alerts.");
    }

    @Test
    void shouldReturnFormattedAlertList() {
        UserAlertResponse alert1 = new UserAlertResponse(1L, "BTC",
                new BigDecimal("50000"), "ABOVE", true, Instant.now());
        UserAlertResponse alert2 = new UserAlertResponse(2L, "ETH",
                new BigDecimal("4000"), "BELOW", true, Instant.now());

        when(alertServiceClient.getUserAlerts(chatId))
                .thenReturn(List.of(alert1, alert2));

        handlerBotsCommand.handleListCommand(chatId);

        String expected = "*Your alerts:*\n\n" +
                "BTC  ABOVE $50000\n" +
                "ETH  BELOW $4000\n";
        verify(cryptoBot).sendMessage(chatId, expected);
    }

    @Test
    void shouldMarkPausedAlertInList() {
        UserAlertResponse paused = new UserAlertResponse(1L, "SOL",
                new BigDecimal("200"), "ABOVE", false, Instant.now());

        when(alertServiceClient.getUserAlerts(chatId))
                .thenReturn(List.of(paused));

        handlerBotsCommand.handleListCommand(chatId);

        String expected = "*Your alerts:*\n\n" +
                "SOL  ABOVE $200  *paused*\n";
        verify(cryptoBot).sendMessage(chatId, expected);
    }

    @Test
    void handleStart() {
        handlerBotsCommand.handleStart(chatId);

        verify(cryptoBot).sendMessage(eq(chatId), contains("Crypto Monitor Bot"));
    }

    @Test
    void handleCommands() {
        handlerBotsCommand.handleCommands(chatId);

        verify(cryptoBot).sendMessage(eq(chatId), contains("*Commands*"));
    }
}
