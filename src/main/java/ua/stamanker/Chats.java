package ua.stamanker;

import ua.stamanker.entities.ChatInfo;
import ua.stamanker.entities.ChatInfoUknown;

import java.util.HashMap;
import java.util.Map;

public class Chats {

    private Map<Long, ChatInfo> chatIdNames = new HashMap<>();
    private Map<Integer, Long> chat2PostNextMessageBy = new HashMap<>();

    public Chats() {
        init();
    }

    public Chats storePost2ChatSetting(int userId, String chatId) {
        long chatIdNum = Long.parseLong(chatId.trim());
        if(!chatIdNames.containsKey(chatIdNum)) {
            throw new IllegalArgumentException("wrong chat: " + chatIdNum);
        }
        chat2PostNextMessageBy.put(userId, chatIdNum);
        return this;
    }

    private void init() {
    }

    public long getPrivateChatId() {
        return chatIdNames.values().stream().filter(c->c.isBotPrivateChat).findFirst().map(c->c.chatId).orElse(-1L);
    }

    public long getDefaultChatId() {
        return chatIdNames.values().stream().filter(c->c.isDefault).findFirst().map(c->c.chatId).orElse(-1L);
    }

    public String getChat2RePostAsStr(int userId) {
        Long chatId = chat2PostNextMessageBy.remove(userId);
        if(chatId != null) {
            return chatId+"";
        }
        return getDefaultChatId()+"";
    }

    public String getChatNameById(long id) {
        return chatIdNames.getOrDefault(id, new ChatInfoUknown()).chatName;
    }

    public boolean isPrivateBotChat(Long chatId) {
        return chatId == getPrivateChatId();
    }
}
