package com.sorokaandriy.price_alert.service;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.entity.UserAlert;
import com.sorokaandriy.price_alert.exception.UserAlertNotFoundException;
import com.sorokaandriy.price_alert.repository.UserAlertRepository;
import com.sorokaandriy.price_alert.service.mapper.UserAlertMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAlertService {

    private final UserAlertRepository repository;
    private final UserAlertMapper mapper;

    public UserAlertResponse createUserAlert(@Valid UserAlertRequest request) {
        UserAlert userAlert = repository.save(mapper.fromUserAlertRequestToUserAlert(request));
        return mapper.fromUserAlertToUserAlertResponse(userAlert);


    }

    public UserAlertResponse updateUserAlert(@Valid UserAlertRequest request, Long chatId) {
        UserAlert userAlert = repository.findByChatIdAndSymbol(chatId, request.getSymbol())
                .orElseThrow(() -> new UserAlertNotFoundException("UserAlert with chatId " + chatId + " not found"));

        userAlert.setTargetPrice(request.getTargetPrice());
        userAlert.setDirection(request.getDirection());

        repository.save(userAlert);
        return mapper.fromUserAlertToUserAlertResponse(userAlert);
    }
}
