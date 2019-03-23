package ua.stamanker.entities;

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
    }

    public Settings validate() {
        if(botUsername==null || botToken == null) {
            throw new IllegalStateException("Error: settings are not defined");
        }
        return this;
    }

}
