package ua.stamanker;

import java.util.HashMap;
import java.util.Map;

public class Chats {

    public static final long botPrivatechatId = 31540560L;
    //    public static final long botTestChatId = -1001194141871L;
//    public static final long botTestChatId = -1001122538376L;//rsprtr
    public static final long chatId2Post2 = -1001287571102L;//nottify

    private Map<Long, String> chatIdNames = new HashMap<>();

    public Chats() {
        init();
    }

    private void init() {
        chatIdNames.put(-1001287571102L, "Nuttify");
        chatIdNames.put(-1001122538376L, "rsprtr");
        chatIdNames.put(-1001194141871L, "test-ntf");
    }

    public String getChatById(long id) {
        return chatIdNames.getOrDefault(id, "unknown");
    }

}
