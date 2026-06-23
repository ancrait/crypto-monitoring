package com.sorokaandriy.price_alert.service.mapper;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.entity.UserAlert;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserAlertMapper {

    public UserAlert fromUserAlertRequestToUserAlert(UserAlertRequest request){
        return UserAlert.builder()
                .chatId(request.getChatId())
                .symbol(request.getSymbol())
                .targetPrice(request.getTargetPrice())
                .direction(request.getDirection())
                .enabled(true)
                .triggered(false)
                .createdAt(Instant.now())
                .build();
    }

    public UserAlertResponse fromUserAlertToUserAlertResponse(UserAlert userAlert) {
        return UserAlertResponse.builder()
                .id(userAlert.getId())
                .symbol(userAlert.getSymbol())
                .targetPrice(userAlert.getTargetPrice())
                .direction(userAlert.getDirection())
                .createdAt(userAlert.getCreatedAt())
                .build();
    }
}
