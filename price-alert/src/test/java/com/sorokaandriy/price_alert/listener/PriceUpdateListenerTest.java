package com.sorokaandriy.price_alert.listener;

import com.sorokaandriy.price_alert.dto.PriceUpdate;
import com.sorokaandriy.price_alert.dto.UserAlertNotification;
import com.sorokaandriy.price_alert.entity.UserAlert;
import com.sorokaandriy.price_alert.entity.enumeration.Direction;
import com.sorokaandriy.price_alert.repository.UserAlertRepository;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceUpdateListenerTest {

    @Mock
    private UserAlertRepository repository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private PriceUpdateListener priceUpdateListener;

    @Test
    void shouldTriggerAlertWhenPriceAboveTargetForAboveDirection() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("51000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("50000.00"), Direction.ABOVE, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, times(1)).delete(userAlert);

        ArgumentCaptor<UserAlertNotification> captor = ArgumentCaptor.forClass(UserAlertNotification.class);
        verify(redisTemplate).convertAndSend(eq("alerts:notification"), captor.capture());

        UserAlertNotification notification = captor.getValue();
        assertEquals(132117L, notification.getChatId());
        assertEquals("BTC", notification.getSymbol());
        assertEquals(new BigDecimal("51000.00"), notification.getUpdatedPrice());
        assertEquals(new BigDecimal("50000.00"), notification.getTargetPrice());
        assertEquals(Direction.ABOVE, notification.getDirection());

    }


    @Test
    void shouldNotTriggerAlertWhenEqualsPriceForAboveDirection() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("50000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("50000.00"), Direction.ABOVE, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, never()).delete(any(UserAlert.class));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());

    }

    @Test
    void shouldNotTriggerAlertWhenPriceBelowTargetForAboveDirection() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("40000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("50000.00"), Direction.ABOVE, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, never()).delete(any(UserAlert.class));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());


    }

    @Test
    void shouldTriggerAlertWhenPriceBelowTargetForBelowDirection(){

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("49000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("50000.00"), Direction.BELOW, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, times(1)).delete(userAlert);

        ArgumentCaptor<UserAlertNotification> captor = ArgumentCaptor.forClass(UserAlertNotification.class);
        verify(redisTemplate).convertAndSend(eq("alerts:notification"), captor.capture());

        UserAlertNotification notification = captor.getValue();
        assertEquals(132117L, notification.getChatId());
        assertEquals("BTC", notification.getSymbol());
        assertEquals(new BigDecimal("49000.00"), notification.getUpdatedPrice());
        assertEquals(new BigDecimal("50000.00"), notification.getTargetPrice());
        assertEquals(Direction.BELOW, notification.getDirection());
    }


    @Test
    void shouldNotTriggerAlertWhenEqualsPriceForBelowDirection() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("50000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("50000.00"), Direction.BELOW, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, never()).delete(any(UserAlert.class));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());

    }

    @Test
    void shouldNotTriggerAlertWhenPriceAboveTargetForBelowDirection() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("50000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("40000.00"), Direction.BELOW, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, never()).delete(any(UserAlert.class));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());


    }


    @Test
    void shouldNotTriggerAlertWhenEnabledFalse() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("50000.00"),
                "BINANCE", Instant.now());

        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("40000.00"), Direction.BELOW, false, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(userAlert));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, never()).delete(any(UserAlert.class));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());


    }



    @Test
    void shouldNotTriggerWithEmptyRepository() {

        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("50000.00"),
                "BINANCE", Instant.now());

//        UserAlert userAlert = new UserAlert(12L, 132117L, "BTC",
//                new BigDecimal("40000.00"), Direction.BELOW, false, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(Collections.emptyList());

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository, never()).delete(any(UserAlert.class));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());


    }


    @Test
    void shouldTriggerBothAlertsWhenPriceBelowBothTargets() {
        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("30000.00"),
                "BINANCE", Instant.now());

        UserAlert alert1 = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("40000.00"), Direction.BELOW, true, Instant.now());

        UserAlert alert2 = new UserAlert(13L, 999999L, "BTC",
                new BigDecimal("42000.00"), Direction.BELOW, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(alert1, alert2));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository).delete(alert1);
        verify(repository).delete(alert2);

        ArgumentCaptor<UserAlertNotification> captor = ArgumentCaptor.forClass(UserAlertNotification.class);
        verify(redisTemplate, times(2)).convertAndSend(eq("alerts:notification"), captor.capture());

        List<UserAlertNotification> notifications = captor.getAllValues();
        assertEquals(2, notifications.size());

        UserAlertNotification notification = notifications.getFirst();
        assertEquals(132117L, notification.getChatId());
        assertEquals("BTC", notification.getSymbol());
        assertEquals(new BigDecimal("30000.00"), notification.getUpdatedPrice());
        assertEquals(new BigDecimal("40000.00"), notification.getTargetPrice());
        assertEquals(Direction.BELOW, notification.getDirection());


        UserAlertNotification notification2 = notifications.get(1);
        assertEquals(999999L, notification2.getChatId());
        assertEquals("BTC", notification2.getSymbol());
        assertEquals(new BigDecimal("30000.00"), notification2.getUpdatedPrice());
        assertEquals(new BigDecimal("42000.00"), notification2.getTargetPrice());
        assertEquals(Direction.BELOW, notification2.getDirection());
    }



    @Test
    void shouldTriggerBothAlertsWhenPriceAboveBothTargets() {
        PriceUpdate update = new PriceUpdate("BTC", new BigDecimal("60000.00"),
                "BINANCE", Instant.now());

        UserAlert alert1 = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("40000.00"), Direction.ABOVE, true, Instant.now());

        UserAlert alert2 = new UserAlert(13L, 999999L, "BTC",
                new BigDecimal("42000.00"), Direction.ABOVE, true, Instant.now());

        when(repository.findBySymbolAndEnabledTrue(update.getSymbol()))
                .thenReturn(List.of(alert1, alert2));

        priceUpdateListener.handlePriceUpdate(update);

        verify(repository).delete(alert1);
        verify(repository).delete(alert2);

        ArgumentCaptor<UserAlertNotification> captor = ArgumentCaptor.forClass(UserAlertNotification.class);
        verify(redisTemplate, times(2)).convertAndSend(eq("alerts:notification"), captor.capture());

        List<UserAlertNotification> notifications = captor.getAllValues();
        assertEquals(2, notifications.size());

        UserAlertNotification notification = notifications.getFirst();
        assertEquals(132117L, notification.getChatId());
        assertEquals("BTC", notification.getSymbol());
        assertEquals(new BigDecimal("60000.00"), notification.getUpdatedPrice());
        assertEquals(new BigDecimal("40000.00"), notification.getTargetPrice());
        assertEquals(Direction.ABOVE, notification.getDirection());


        UserAlertNotification notification2 = notifications.get(1);
        assertEquals(999999L, notification2.getChatId());
        assertEquals("BTC", notification2.getSymbol());
        assertEquals(new BigDecimal("60000.00"), notification2.getUpdatedPrice());
        assertEquals(new BigDecimal("42000.00"), notification2.getTargetPrice());
        assertEquals(Direction.ABOVE, notification2.getDirection());
    }










}
