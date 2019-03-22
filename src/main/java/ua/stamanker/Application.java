package ua.stamanker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Application {

    public static ObjectMapper OBJECTMAPPER;

    public static void main(String[] args) {
        OBJECTMAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Chats chats = new Chats();
        try {
            telegramBotsApi.registerBot(new NtfLongPollBot(chats));
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
