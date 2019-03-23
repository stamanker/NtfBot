package ua.stamanker.entities;

import java.util.List;

public class ChatInfo {

    public String chatName;
    public long chatId;
    public boolean isDefault;
    public boolean isBotPrivateChat;
    public List<String> buttons;

    public ChatInfo() {
    }

    public ChatInfo(String chatName, long chatId, List<String> buttons, boolean isDefault, boolean isBotPrivateChat) {
        this.chatName = chatName;
        this.chatId = chatId;
        this.isDefault = isDefault;
        this.isBotPrivateChat = isBotPrivateChat;
        this.buttons = buttons;
    }

    @Override
    public String toString() {
        return "ChatInfo{" +
                "chatName='" + chatName + '\'' +
                ", chatId=" + chatId +
                ", isDefault=" + isDefault +
                ", isBotPrivateChat=" + isBotPrivateChat +
                '}';
    }
}
