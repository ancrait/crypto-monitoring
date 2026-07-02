package com.sorokaandriy.telegram_bot.handler;

import com.sorokaandriy.telegram_bot.bot.CryptoBot;
import com.sorokaandriy.telegram_bot.client.AlertServiceClient;
import com.sorokaandriy.telegram_bot.dto.UserAlertRequest;
import com.sorokaandriy.telegram_bot.dto.UserAlertResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class HandlerBotsCommand {

    private static final Set<String> ALLOWED_SYMBOLS = Set.of("BTC", "ETH", "SOL");

    private final AlertServiceClient alertServiceClient;
    private final CryptoBot cryptoBot;

    public HandlerBotsCommand(AlertServiceClient alertServiceClient, @Lazy CryptoBot cryptoBot) {
        this.alertServiceClient = alertServiceClient;
        this.cryptoBot = cryptoBot;
    }

    public void handleStart(Long chatId) {
        String msg = """
                *Crypto Monitor Bot*

                Track crypto prices and get instant alerts when your targets are hit.

                *Commands:*

                /track \\<coin\\> \\<price\\> \\[below\\] — create alert
                /list — view your alerts
                /update \\<coin\\> \\<price\\> \\<direction\\> — update alert
                /untrack \\<coin\\> \\<price\\> — delete alert
                /pause \\<coin\\> \\<price\\> — pause alert
                /resume \\<coin\\> \\<price\\> — resume alert
                /commands — detailed help
                """;
        cryptoBot.sendMessage(chatId, msg);
    }

    public void handleCommands(Long chatId) {
        String msg = """
                *Commands*

                */track \\<coin\\> \\<price\\> \\[above or below\\]*
                Create a price alert. You can choose when it will work, when it is below the price (below) or above (above).
                If direction is not specified, it will default to "above". 
                _Example:_ /track BTC 100000 above
                _Example:_ /track ETH 5000 below

                */list*
                Show your active alerts and paused alerts.

                */update \\<coin\\> \\<exist price\\> \\<new price\\> \\<above/below\\>*
                Change target price or direction.
                _Example:_ /update BTC 95000 100000 above

                */untrack \\<coin\\> \\<price\\>*
                Delete an alert permanently.
                _Example:_ /untrack SOL 200
  
                */pause \\<coin\\> \\<price\\>*
                Temporarily disable an alert.
                _Example:_ /pause BTC 100000

                */resume \\<coin\\> \\<price\\>*
                Re-enable a paused alert.
                _Example:_ /resume BTC 100000

                *Available coins:* BTC, ETH, SOL
                *Price format:* up to 12 digits before dot, up to 8 after
                """;
        cryptoBot.sendMessage(chatId, msg);
    }

    public void handleTrackCommand(Long chatId, String text) {
        String[] parts = normalizeText(text);
        if (parts.length < 3) {
            cryptoBot.sendMessage(chatId, "Usage: /track BTC 100000 [below]");
            return;
        }

        String symbol = parts[1].toUpperCase();
        if (!ALLOWED_SYMBOLS.contains(symbol)) {
            cryptoBot.sendMessage(chatId, "Unknown coin: " + symbol + ". Available: BTC, ETH, SOL");
            return;
        }

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            cryptoBot.sendMessage(chatId, "Invalid price format: " + parts[2]);
            return;
        }
        if (targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
            cryptoBot.sendMessage(chatId, "Price must be greater than 0");
            return;
        }

        String direction;
        if (parts.length >= 4) {
            if ("below".equalsIgnoreCase(parts[3])) {
                direction = "BELOW";
            } else if ("above".equalsIgnoreCase(parts[3])) {
                direction = "ABOVE";
            } else {
                cryptoBot.sendMessage(chatId, "Direction must be 'above' or 'below', got: " + parts[3]);
                return;
            }
        } else {
            direction = "ABOVE";
        }

        UserAlertRequest request = UserAlertRequest.builder()
                .chatId(chatId)
                .symbol(symbol)
                .targetPrice(targetPrice)
                .direction(direction)
                .build();

        try {
            alertServiceClient.createAlert(request);
        } catch (Exception e) {
            cryptoBot.sendMessage(chatId, "Alert already exists: " + symbol + " at $" + targetPrice);
            return;
        }

        cryptoBot.sendMessage(chatId, symbol + " — " + direction + " $" + targetPrice);
    }

    public void handleListCommand(Long chatId) {
        List<UserAlertResponse> alerts = alertServiceClient.getUserAlerts(chatId);

        if (alerts.isEmpty()) {
            cryptoBot.sendMessage(chatId, "You have no active alerts.");
            return;
        }

        StringBuilder sb = new StringBuilder("*Your alerts:*\n\n");
        for (UserAlertResponse alert : alerts) {
            sb.append(alert.getSymbol())
                    .append("  ")
                    .append(alert.getDirection())
                    .append(" $").append(alert.getTargetPrice());
            if (!alert.getEnabled()) {
                sb.append("  *paused*");
            }
            sb.append("\n");
        }
        cryptoBot.sendMessage(chatId, sb.toString());
    }

    // /exist BTC 60000 direction
    public void handleExistCommand(Long chatId, String text){
        String[] parts = normalizeText(text);
        if (parts.length < 4) {
            cryptoBot.sendMessage(chatId, "Usage: /get BTC 95000 above");
            return;
        }

        String symbol = parts[1].toUpperCase();
        if (!ALLOWED_SYMBOLS.contains(symbol)) {
            cryptoBot.sendMessage(chatId, "Unknown coin: " + symbol + ". Available: BTC, ETH, SOL");
            return;
        }

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            cryptoBot.sendMessage(chatId, "Invalid price: " + parts[2]);
            return;
        }

        String directionRaw = parts[3].toUpperCase();
        if (!directionRaw.equals("ABOVE") && !directionRaw.equals("BELOW")) {
            cryptoBot.sendMessage(chatId, "Direction must be 'above' or 'below', got: " + parts[3]);
            return;
        }

        try {
            UserAlertResponse alert = alertServiceClient.getUserAlert(chatId, symbol, targetPrice);
            cryptoBot.sendMessage(chatId, "Alert exists: " + alert.getSymbol() + " "
                    + alert.getDirection() + " $" + alert.getTargetPrice());
        } catch (Exception e) {
            cryptoBot.sendMessage(chatId, "Alert not found: " + symbol + " " + directionRaw + " $" + targetPrice);
        }
    }

    // /update BTC 60000 59000 direction
    public void handleUpdateCommand(Long chatId, String text) {
        String[] parts = normalizeText(text);
        if (parts.length < 5) {
            cryptoBot.sendMessage(chatId, "Usage: /update BTC 95000 100000 above");
            return;
        }

        String symbol = parts[1].toUpperCase();
        if (!ALLOWED_SYMBOLS.contains(symbol)) {
            cryptoBot.sendMessage(chatId, "Unknown coin: " + symbol + ". Available: BTC, ETH, SOL");
            return;
        }

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            cryptoBot.sendMessage(chatId, "Invalid price: " + parts[2]);
            return;
        }


        BigDecimal newTargetPrice;
        try {
            newTargetPrice = new BigDecimal(parts[3]);
        } catch (NumberFormatException e) {
            cryptoBot.sendMessage(chatId, "Invalid price: " + parts[3]);
            return;
        }

        String directionRaw = parts[4].toUpperCase();
        if (!directionRaw.equals("ABOVE") && !directionRaw.equals("BELOW")) {
            cryptoBot.sendMessage(chatId, "Direction must be 'above' or 'below', got: " + parts[4]);
            return;
        }

        UserAlertRequest request = UserAlertRequest.builder()
                .chatId(chatId)
                .symbol(symbol)
                .targetPrice(newTargetPrice)
                .direction(directionRaw)
                .build();


        try {
            alertServiceClient.updateUserAlert(request, chatId, symbol, targetPrice);
        } catch (Exception e) {
            cryptoBot.sendMessage(chatId, "Alert with symbol " + symbol + " and target price " + targetPrice +
                    " not found");
            return;
        }

        cryptoBot.sendMessage(chatId, symbol + " updated: " + directionRaw + " $" + newTargetPrice);
    }

    public void handleUntrackCommand(Long chatId, String text) {
        String[] parts = normalizeText(text);
        if (parts.length < 3) {
            cryptoBot.sendMessage(chatId, "Usage: /untrack BTC 100000");
            return;
        }

        String symbol = parts[1].toUpperCase();
        if (!ALLOWED_SYMBOLS.contains(symbol)) {
            cryptoBot.sendMessage(chatId, "Unknown coin: " + symbol + ". Available: BTC, ETH, SOL");
            return;
        }

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            cryptoBot.sendMessage(chatId, "Invalid price: " + parts[2]);
            return;
        }

        try {
            alertServiceClient.deleteUserAlert(chatId, symbol, targetPrice);
        } catch (Exception e) {
            cryptoBot.sendMessage(chatId, "Alert with symbol " + symbol + " and target price " + targetPrice +
                    " doesnt exist");
            return;
        }

        cryptoBot.sendMessage(chatId, symbol + " removed");
    }


    public void handlePauseResumeCommand(Long chatId, String text) {
        String[] parts = normalizeText(text);
        if (parts.length < 3) {
            cryptoBot.sendMessage(chatId, "Usage: /pause BTC 100000  or  /resume BTC 100000");
            return;
        }


        String symbol = parts[1].toUpperCase();
        if (!ALLOWED_SYMBOLS.contains(symbol)) {
            cryptoBot.sendMessage(chatId, "Unknown coin: " + symbol + ". Available: BTC, ETH, SOL");
            return;
        }

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            cryptoBot.sendMessage(chatId, "Invalid price: " + parts[2]);
            return;
        }

        try {
            Boolean isEnabled = alertServiceClient.changeEnabled(chatId, symbol, targetPrice);
            String status = isEnabled ? "resumed" : "paused";
            cryptoBot.sendMessage(chatId, symbol + " " + status);
        } catch (Exception e) {
            cryptoBot.sendMessage(chatId, "Alert with symbol " + symbol + " and target price " + targetPrice +
                    " doesnt exist");
        }

    }


    private String[] normalizeText(String text) {
        return text.trim().replaceAll("\\s+", " ").split(" ");
    }
}
