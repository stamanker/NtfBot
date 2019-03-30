package ua.stamanker.tgbt.ntf.entities;

import java.util.HashMap;
import java.util.Map;

public class Settings {

    public String botUsername;
    public String botToken;

    public Map<Long, ChatInfo> chatIdNames = new HashMap<>();

    public Settings() {
        init();
    }

    public void init() {
//        botToken = "675917208:AAExq8Jt51Qefj4JSnU0Zu8L3ZA0a6MGZ6E";
//        botUsername = "Nuttyfi";
//        chatIdNames.put(31540560L,       new ChatInfo("botPrivateChat", 31540560L, false, true));
//        chatIdNames.put(-1001287571102L, new ChatInfo("Nuttify", -1001287571102L, true, false));
//        chatIdNames.put(-1001122538376L, new ChatInfo("rsprtr", -1001122538376L, false, false));
//        chatIdNames.put(-1001194141871L, new ChatInfo("test-ntf", -1001194141871L, false, false));
    }

    public Settings validate() {
        if(botUsername==null || botToken == null) {
            throw new IllegalStateException("Error: settings are not defined");
        }
        System.out.println(toString());
        return this;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "botUsername='" + botUsername + '\'' +
                ", botToken='" + botToken + '\'' +
                ", chatIdNames=" + chatIdNames +
                '}';
    }
}
