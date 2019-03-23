package ua.stamanker;

import ua.stamanker.entities.ChatInfoUknown;
import ua.stamanker.entities.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chats {

    public static class X {
        public Long chatId;
        public List<String> buttons;
        public X(Long chatId, List<String> buttons) {
            this.chatId = chatId;
            this.buttons = buttons;
        }
        @Override
        public String toString() { return "X{ chatId=" + chatId + ", smiles=" + buttons + '}'; }
    }

    private Map<Integer, X> chat2PostNextMessageBy = new HashMap<>();
    private Settings settings;

    public Chats(Settings settings) {
        this.settings = settings;
    }

    public Chats store2Post(int userId, Long chatIdNum, List<String> buttons) {
        X chat2RePost = getChat2RePost(userId);
        if(chatIdNum!=null && !settings.chatIdNames.containsKey(chatIdNum)) {
            throw new IllegalArgumentException("Wrong chat: " + chatIdNum);
        }
        if(buttons!=null) {
            chat2RePost.buttons = buttons;
        }
        if(chatIdNum!=null) {
            chat2RePost.chatId = chatIdNum;
        }
        System.out.println("chat2RePost = " + chat2RePost);
        return this;
    }

    public boolean storeDefaultButtons(Integer userId, Long chatId, List<String> buttons) {
        settings.chatIdNames.get(chatId).buttons = buttons;
        new FileWorker().saveSettings(settings);
        return true;
    }

    public long getPrivateChatId() {
        return settings.chatIdNames.values().stream().filter(c->c.isBotPrivateChat).findFirst().map(c->c.chatId).orElse(-1L);
    }

    public long getDefaultChatId() {
        return settings.chatIdNames.values().stream().filter(c->c.isDefault).findFirst().map(c->c.chatId).orElse(-1L);
    }

    public X getChat2RePost(int userId) {
        X x = chat2PostNextMessageBy.computeIfAbsent(userId, u ->
                new X(
                        getDefaultChatId(),
                        settings.chatIdNames.get(getDefaultChatId()).buttons
                )
        );
        System.out.println("x1 = " + x);
        return x;
    }

    public X getChat2RePostAndRemove(int userId) {
        X chat2RePost = getChat2RePost(userId);
        chat2PostNextMessageBy.remove(userId);
        return chat2RePost;
    }

    public String getChatNameById(String id) {
        return getChatNameById(Long.parseLong(id.trim()));
    }

    public String getChatNameById(long id) {
        return settings.chatIdNames.getOrDefault(id, new ChatInfoUknown()).chatName;
    }

    public boolean isPrivateBotChat(Long chatId) {
        return chatId == getPrivateChatId();
    }
}
