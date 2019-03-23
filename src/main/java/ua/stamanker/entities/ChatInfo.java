package ua.stamanker.entities;

public class ChatInfo {

    public String chatName;
    public long chatId;
    public boolean isDefault;
    public boolean isBotPrivateChat;

    public ChatInfo() {
    }

    public ChatInfo(String chatName, long chatId, boolean isDefault, boolean isBotPrivateChat) {
        this.chatName = chatName;
        this.chatId = chatId;
        this.isDefault = isDefault;
        this.isBotPrivateChat = isBotPrivateChat;
    }
}
