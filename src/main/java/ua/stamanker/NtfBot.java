package ua.stamanker;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class NtfBot extends TelegramWebhookBot {

    @Override
    public String getBotToken() {
        return "675917208:AAExq8Jt51Qefj4JSnU0Zu8L3ZA0a6MGZ6E";
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        System.out.println("update = " + update);
        return null;
    }

    @Override
    public String getBotUsername() {
        return "Nuttyfi";
    }

    @Override
    public String getBotPath() {
        return null;
    }
}
