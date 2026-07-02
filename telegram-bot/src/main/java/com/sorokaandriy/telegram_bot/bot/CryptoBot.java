package com.sorokaandriy.telegram_bot.bot;

import com.sorokaandriy.telegram_bot.client.AlertServiceClient;
import com.sorokaandriy.telegram_bot.dto.UserAlertRequest;
import com.sorokaandriy.telegram_bot.dto.UserAlertResponse;
import com.sorokaandriy.telegram_bot.handler.HandlerBotsCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoBot extends TelegramLongPollingBot {

    private final HandlerBotsCommand handlerBotsCommand;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @PostConstruct
    public void init() throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (text.startsWith("/start")) {
                handlerBotsCommand.handleStart(chatId);
            }

            if (text.startsWith("/commands")){
                handlerBotsCommand.handleCommands(chatId);
            }

            if (text.startsWith("/track")) {
                handlerBotsCommand.handleTrackCommand(chatId, text);
            }

            if (text.startsWith("/update")){
                handlerBotsCommand.handleUpdateCommand(chatId, text);
            }

            if (text.startsWith("/list")) {
                handlerBotsCommand.handleListCommand(chatId);
            }

            if (text.startsWith("/untrack")) {
                handlerBotsCommand.handleUntrackCommand(chatId, text);
            }

            if (text.startsWith("/pause")) {
                handlerBotsCommand.handlePauseResumeCommand(chatId, text);
            }
            if (text.startsWith("/resume")) {
                handlerBotsCommand.handlePauseResumeCommand(chatId, text);
            }

        }
    }



    public void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage());
        }
    }
}
