package com.sorokaandriy.telegram_bot.listener;

import com.sorokaandriy.telegram_bot.bot.CryptoBot;
import com.sorokaandriy.telegram_bot.client.AlertServiceClient;
import com.sorokaandriy.telegram_bot.dto.AlertNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertNotificationListener {

    private final CryptoBot cryptoBot;
    private final AlertServiceClient alertServiceClient;

    public void handlePriceNotification(AlertNotification notification) {
        String msg = notification.getSymbol() + " ALERT!\n\n"
                + "Price: $" + notification.getUpdatedPrice() + "\n"
                + "Target: $" + notification.getTargetPrice() + "\n"
                + "Direction: " + notification.getDirection() + "\n"
                + "Time: " + notification.getUpdatedAt()
                .atZone(ZoneId.of("Europe/Kyiv"))
                .format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));

        cryptoBot.sendMessage(notification.getChatId(), msg);

        alertServiceClient.deleteUserAlert(notification.getChatId(),
                notification.getSymbol(), notification.getTargetPrice());



    }
}
