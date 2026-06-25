package com.sorokaandriy.price_alert.service;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.entity.UserAlert;
import com.sorokaandriy.price_alert.exception.UserAlertNotFoundException;
import com.sorokaandriy.price_alert.repository.UserAlertRepository;
import com.sorokaandriy.price_alert.service.mapper.UserAlertMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAlertService {

    private final UserAlertRepository repository;
    private final UserAlertMapper mapper;
    private final RedisTemplate<String,Object> redisTemplate;
    private final static String REDIS_KEY = "alerts:";


    public UserAlertResponse createUserAlert(@Valid UserAlertRequest request) {
        UserAlert userAlert = repository.save(mapper.fromUserAlertRequestToUserAlert(request));
        UserAlertResponse response = mapper.fromUserAlertToUserAlertResponse(userAlert);
        redisTemplate.opsForValue()
                .set(REDIS_KEY + request.getChatId() +":" + request.getSymbol(), response, Duration.ofMinutes(3));
        return response;


    }

    public UserAlertResponse updateUserAlert(@Valid UserAlertRequest request, Long chatId, String symbol) {
        UserAlert userAlert = repository.findByChatIdAndSymbol(chatId, symbol)
                .orElseThrow(() -> new UserAlertNotFoundException("UserAlert with chatId " + chatId + " not found"));

        userAlert.setTargetPrice(request.getTargetPrice());
        userAlert.setDirection(request.getDirection());

        repository.save(userAlert);
        redisTemplate.delete(REDIS_KEY + request.getChatId() +":" + request.getSymbol());
        return mapper.fromUserAlertToUserAlertResponse(userAlert);
    }

    public void deleteUserAlert(Long chatId, String symbol) {
        UserAlert userAlert = repository.findByChatIdAndSymbol(chatId, symbol)
                .orElseThrow(() -> new UserAlertNotFoundException("UserAlert with chatId " + chatId + " not found"));

        redisTemplate.delete(REDIS_KEY + chatId + ":" + symbol);
        repository.delete(userAlert);
    }

    public UserAlertResponse getUserAlert(Long chatId, String symbol) {
        String key = REDIS_KEY + chatId + ":" + symbol;

        UserAlertResponse cached = (UserAlertResponse) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        UserAlert userAlert = repository.findByChatIdAndSymbol(chatId, symbol)
                .orElseThrow(() -> new UserAlertNotFoundException("UserAlert with chatId " + chatId + " not found"));

        UserAlertResponse response = mapper.fromUserAlertToUserAlertResponse(userAlert);
        redisTemplate.opsForValue().set(key, response, Duration.ofMinutes(3));
        return response;
    }

    public List<UserAlertResponse> getUserAlerts(Long chatId) {
        List<UserAlert> userAlertList = repository.findByChatId(chatId);

        return userAlertList.stream().map(userAlert ->
                mapper.fromUserAlertToUserAlertResponse(userAlert)).toList();

    }

    public void changeEnabled(Long chatId, String symbol) {
        UserAlert userAlert = repository.findByChatIdAndSymbol(chatId, symbol)
                .orElseThrow(() -> new UserAlertNotFoundException("UserAlert with chatId " + chatId + " and symbol " +
                        symbol + " not found"));

        userAlert.setEnabled(!userAlert.getEnabled());
        repository.save(userAlert);
        redisTemplate.delete(REDIS_KEY + chatId + ":" + symbol);
    }
}
