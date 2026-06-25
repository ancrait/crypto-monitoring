package com.sorokaandriy.price_fetcher.service;

import com.sorokaandriy.price_fetcher.client.BinanceClient;
import com.sorokaandriy.price_fetcher.dto.BinancePriceResponse;
import com.sorokaandriy.price_fetcher.entity.Coin;
import com.sorokaandriy.price_fetcher.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class PriceFetcherService {

    private final BinanceClient binanceClient;
    private final CoinRepository coinRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.binance.symbols}")
    private String symbols;

    @Scheduled(fixedRateString = "5000")
    public void fetchPrices() {
        List<String> symbolList = Arrays.asList(symbols.split(","));

        symbolList.forEach(s ->{
            try {
                BinancePriceResponse response = binanceClient.fetchPrice(s);

                Coin coin = Coin.builder()
                        .symbol(s.replace("USDT",""))
                        .priceUsd(new BigDecimal(response.getPrice()))
                        .source("BINANCE")
                        .timestamp(Instant.now())
                        .build();

                coinRepository.save(coin);

                redisTemplate.convertAndSend("prices:updates", coin);

                log.info("Fetched {}: ${}", coin.getSymbol(), coin.getPriceUsd());

            } catch (Exception e) {
                log.error("Failed to fetch price for {}: {}", s, e.getMessage());
            }
        });
    }
}
