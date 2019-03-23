package ua.stamanker;

import ua.stamanker.entities.ChatInfoUknown;
import ua.stamanker.entities.Settings;

import java.util.HashMap;
import java.util.Map;

public class Chats {

    private Map<Integer, Long> chat2PostNextMessageBy = new HashMap<>();
    private Settings settings;

    public Chats(Settings settings) {
        this.settings = settings;
    }

    public Chats storePost2ChatSetting(int userId, String chatId) {
        long chatIdNum = Long.parseLong(chatId.trim());
        if(!settings.chatIdNames.containsKey(chatIdNum)) {
            throw new IllegalArgumentException("wrong chat: " + chatIdNum);
        }
        chat2PostNextMessageBy.put(userId, chatIdNum);
        return this;
    }

    public long getPrivateChatId() {
        return settings.chatIdNames.values().stream().filter(c->c.isBotPrivateChat).findFirst().map(c->c.chatId).orElse(-1L);
    }

    public long getDefaultChatId() {
        return settings.chatIdNames.values().stream().filter(c->c.isDefault).findFirst().map(c->c.chatId).orElse(-1L);
    }

    public String getChat2RePostAsStr(int userId) {
        Long chatId = chat2PostNextMessageBy.remove(userId);
        if(chatId != null) {
            return chatId+"";
        }
        return getDefaultChatId()+"";
    }

    public String getChatNameById(long id) {
        return settings.chatIdNames.getOrDefault(id, new ChatInfoUknown()).chatName;
    }

    public boolean isPrivateBotChat(Long chatId) {
        return chatId == getPrivateChatId();
    }
}
