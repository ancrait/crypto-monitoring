package com.sorokaandriy.price_fetcher.service;

import com.sorokaandriy.price_fetcher.client.BinanceClient;
import com.sorokaandriy.price_fetcher.dto.BinancePriceResponse;
import com.sorokaandriy.price_fetcher.entity.Coin;
import com.sorokaandriy.price_fetcher.repository.CoinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceFetcherServiceTest {

    @Mock
    private BinanceClient binanceClient;

    @Mock
    private CoinRepository coinRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private PriceFetcherService priceFetcherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(priceFetcherService, "symbols", "BTCUSDT,ETHUSDT,SOLUSDT");
    }

    @Test
    void shouldFetchAndSavePriceForEachSymbol() {
        when(binanceClient.fetchPrice("BTCUSDT"))
                .thenReturn(new BinancePriceResponse("BTCUSDT", "50000.00"));
        when(binanceClient.fetchPrice("ETHUSDT"))
                .thenReturn(new BinancePriceResponse("ETHUSDT", "3000.00"));
        when(binanceClient.fetchPrice("SOLUSDT"))
                .thenReturn(new BinancePriceResponse("SOLUSDT", "150.00"));

        priceFetcherService.fetchPrices();

        verify(binanceClient).fetchPrice("BTCUSDT");
        verify(binanceClient).fetchPrice("ETHUSDT");
        verify(binanceClient).fetchPrice("SOLUSDT");
        verify(coinRepository, times(3)).save(any(Coin.class));
        verify(redisTemplate, times(3)).convertAndSend(eq("prices:updates"), any(Coin.class));
    }

    @Test
    void shouldStripUsdtSuffixFromSymbol() {
        when(binanceClient.fetchPrice("BTCUSDT"))
                .thenReturn(new BinancePriceResponse("BTCUSDT", "50000.00"));
        when(binanceClient.fetchPrice("ETHUSDT"))
                .thenReturn(new BinancePriceResponse("ETHUSDT", "3000.00"));
        when(binanceClient.fetchPrice("SOLUSDT"))
                .thenReturn(new BinancePriceResponse("SOLUSDT", "150.00"));

        priceFetcherService.fetchPrices();

        ArgumentCaptor<Coin> captor = ArgumentCaptor.forClass(Coin.class);
        verify(coinRepository, times(3)).save(captor.capture());

        assertEquals("BTC", captor.getAllValues().get(0).getSymbol());
        assertEquals("ETH", captor.getAllValues().get(1).getSymbol());
        assertEquals("SOL", captor.getAllValues().get(2).getSymbol());
    }

    @Test
    void shouldSetPriceSourceAndTimestampOnCoin() {
        when(binanceClient.fetchPrice("BTCUSDT"))
                .thenReturn(new BinancePriceResponse("BTCUSDT", "50000.00"));
        when(binanceClient.fetchPrice("SOLUSDT"))
                .thenReturn(new BinancePriceResponse("SOLUSDT","1200"));

        priceFetcherService.fetchPrices();

        ArgumentCaptor<Coin> captor = ArgumentCaptor.forClass(Coin.class);
        verify(coinRepository,times(2)).save(captor.capture());

        Coin saved = captor.getAllValues().getFirst();
        assertEquals(new BigDecimal("50000.00"), saved.getPriceUsd());
        assertEquals("BINANCE", saved.getSource());
        assertEquals("BTC", saved.getSymbol());

        Coin saved2 = captor.getAllValues().get(1);
        assertEquals(new BigDecimal("1200"), saved2.getPriceUsd());
        assertEquals("BINANCE", saved2.getSource());
        assertEquals("SOL", saved2.getSymbol());
    }

    @Test
    void shouldContinueProcessingWhenBinanceClientFails() {
        when(binanceClient.fetchPrice("BTCUSDT"))
                .thenThrow(new RuntimeException("Binance API unavailable"));
        when(binanceClient.fetchPrice("ETHUSDT"))
                .thenReturn(new BinancePriceResponse("ETHUSDT", "3000.00"));
        when(binanceClient.fetchPrice("SOLUSDT"))
                .thenThrow(new RuntimeException("Network error"));



        priceFetcherService.fetchPrices();

        verify(coinRepository,times(1)).save(any(Coin.class));
        verify(redisTemplate,times(1)).convertAndSend(eq("prices:updates"), any(Coin.class));
    }

    @Test
    void shouldPublishToCorrectRedisChannel() {
        when(binanceClient.fetchPrice("BTCUSDT"))
                .thenReturn(new BinancePriceResponse("BTCUSDT", "50000.00"));

        priceFetcherService.fetchPrices();

        verify(redisTemplate).convertAndSend(eq("prices:updates"), any(Coin.class));
    }

    @Test
    void shouldHandleEmptySymbolInList() {
        ReflectionTestUtils.setField(priceFetcherService, "symbols", "");

        priceFetcherService.fetchPrices();

        verify(binanceClient).fetchPrice("");
        verifyNoInteractions(coinRepository);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldHandleSingleSymbolConfiguration() {
        ReflectionTestUtils.setField(priceFetcherService, "symbols", "BTCUSDT");
        when(binanceClient.fetchPrice("BTCUSDT"))
                .thenReturn(new BinancePriceResponse("BTCUSDT", "50000.00"));

        priceFetcherService.fetchPrices();

        verify(binanceClient, times(1)).fetchPrice(anyString());
        verify(coinRepository, times(1)).save(any(Coin.class));
        verify(redisTemplate, times(1)).convertAndSend(anyString(), any(Coin.class));
    }
}
