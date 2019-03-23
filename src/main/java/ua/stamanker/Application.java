package ua.stamanker;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ua.stamanker.entities.Settings;

public class Application {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Chats chats = new Chats();
        FileWorker fileWorker = new FileWorker();
        Settings settings = fileWorker.readSettings().validate();
        try {
            telegramBotsApi.registerBot(new NtfLongPollBot(chats, fileWorker, settings));
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
