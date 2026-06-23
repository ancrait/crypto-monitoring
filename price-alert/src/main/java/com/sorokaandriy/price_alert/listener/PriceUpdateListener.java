package com.sorokaandriy.price_alert.listener;

import com.sorokaandriy.price_alert.dto.UserAlertNotification;
import com.sorokaandriy.price_alert.dto.PriceUpdate;
import com.sorokaandriy.price_alert.entity.UserAlert;
import com.sorokaandriy.price_alert.entity.enumeration.Direction;
import com.sorokaandriy.price_alert.repository.UserAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateListener {

    private final UserAlertRepository alertRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void handlePriceUpdate(PriceUpdate update) {
        List<UserAlert> alerts = alertRepository.findBySymbolAndEnabledTrue(update.getSymbol());

        alerts.forEach(alert -> {
            boolean shouldNotify = false;

            if (alert.getDirection() == Direction.ABOVE) {
                if (update.getPriceUsd().compareTo(alert.getTargetPrice()) >= 0 && !alert.getTriggered()) {
                    shouldNotify = true;
                    alert.setTriggered(true);
                } else if (update.getPriceUsd().compareTo(alert.getTargetPrice()) < 0 && alert.getTriggered()) {
                    alert.setTriggered(false);
                }
            }
            else if (alert.getDirection() == Direction.BELOW){
                if (update.getPriceUsd().compareTo(alert.getTargetPrice()) <= 0 && !alert.getTriggered()){
                    shouldNotify = true;
                    alert.setTriggered(true);
                }
                else if (update.getPriceUsd().compareTo(alert.getTargetPrice()) > 0 && alert.getTriggered()){
                    alert.setTriggered(false);
                }
            }

            alertRepository.save(alert);

            if (shouldNotify) {
                redisTemplate.convertAndSend("alerts:notification",
                        UserAlertNotification
                                .builder()
                                .chatId(alert.getChatId())
                                .symbol(alert.getSymbol())
                                .updatedPrice(update.getPriceUsd())
                                .targetPrice(alert.getTargetPrice())
                                .direction(alert.getDirection())
                                .triggered(alert.getTriggered())
                                .updatedAt(Instant.now())
                                .build());

                log.info("ALERT TRIGGERED: chatId={} symbol={} price={} target={} direction={}",
                        alert.getChatId(), alert.getSymbol(),
                        update.getPriceUsd(), alert.getTargetPrice(),
                        alert.getDirection());
            }
            });

    }
}
