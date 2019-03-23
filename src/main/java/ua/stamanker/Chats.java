package ua.stamanker;

import java.util.HashMap;
import java.util.Map;

public class Chats {


    private Map<Long, String> chatIdNames = new HashMap<>();

    public Chats() {
        init();
    }

    private void init() {
    }

    public String getChatById(long id) {
        return chatIdNames.getOrDefault(id, "unknown");
    }

}
