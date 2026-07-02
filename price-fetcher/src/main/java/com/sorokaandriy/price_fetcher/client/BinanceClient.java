package com.sorokaandriy.price_fetcher.client;

import com.sorokaandriy.price_fetcher.dto.BinancePriceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceClient {

    private final RestClient restClient;
    private final String apiUrl;

    public BinanceClient(@Value("${app.binance.api-url}") String apiUrl) {
        this.apiUrl = apiUrl;
        this.restClient = RestClient.create();
    }

    public BinancePriceResponse fetchPrice(String symbol) {
        return restClient.get()
                .uri(apiUrl + "?symbol=" + symbol)
                .retrieve()
                .body(BinancePriceResponse.class);
    }


}
