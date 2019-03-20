package ua.stamanker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Application {

    public static ObjectMapper OBJECTMAPPER = new ObjectMapper();

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new NtfLongPollBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
