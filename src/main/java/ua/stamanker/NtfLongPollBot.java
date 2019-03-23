package ua.stamanker;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import ua.stamanker.entities.MsgData;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ua.stamanker.Chats.botPrivatechatId;
import static ua.stamanker.Chats.chatId2Post2;

public class NtfLongPollBot extends TelegramLongPollingBot {

    private Chats chats;

    public NtfLongPollBot(Chats chats) {
        this.chats = chats;
    }

    @Override
    public void onUpdateReceived(Update updateIncome) {
        try {
            processUpdateReceived(updateIncome);
            //getInfoAboutMe();
        } catch (IgnoreException ice) {
            System.err.println(ice.getMessage());
        } catch (Exception ew) {
            ew.printStackTrace();
        }
    }

    private void getInfoAboutMe() {
        CompletableFuture.runAsync(() -> {
            try {
                getMeAsync(new SentCallback<User>() {
                    @Override
                    public void onResult(BotApiMethod<User> method, User response) {
                        System.out.println("method = " + method);
                        System.out.println("response = " + response);
                    }

                    @Override
                    public void onError(BotApiMethod<User> method, TelegramApiRequestException apiException) {
                        System.out.println("method = " + method);
                    }

                    @Override
                    public void onException(BotApiMethod<User> method, Exception exception) {
                        System.out.println("method = " + method);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }}
        );
    }

    public synchronized void processUpdateReceived(Update updateIncome) throws Exception {
        System.out.println(new Date());
        MsgData data;
        Integer messageId;
        Long chatId;
        if(updateIncome.hasMessage()) {
            Message message1 = updateIncome.getMessage();
            String message = message1.getText();
            messageId = updateIncome.getMessage().getMessageId();
            chatId = updateIncome.getMessage().getChatId();
            System.out.println("chatId = " + chatId + " / " + chats.getChatById(chatId));
            data = new MsgData().init();
            if(message1.getChatId() != botPrivatechatId) {
                throw new IgnoreException("# ignore other chat: " + chatId + " / " + chats.getChatById(chatId));
            }
            if (message1.hasPhoto()) {
                int num = message1.getPhoto().size();
                String filePath;
                if(!message1.getPhoto().get(num - 1).hasFilePath()) {
                    filePath = requestFilePath(message1.getPhoto().get(num - 1).getFileId());
                } else {
                    filePath = message1.getPhoto().get(num - 1).getFilePath();
                }
                File f = downloadFile(filePath);
                System.out.println("f = " + f.getAbsolutePath());
                SendPhoto sendPhoto = createSendPhoto(chatId2Post2 + "", f);
                setButtons(data, sendPhoto);
                executeSomething(sendPhoto);
                if (!f.delete()) {
                    System.out.println("File not deleted: " + f.getAbsolutePath());
                }
            } else {
                SendMessage sendMessage = createSendMessage(chatId2Post2 + "", message);
                setButtons(data, sendMessage);
                executeSomething(sendMessage);
            }
        } else if (updateIncome.hasCallbackQuery()) {
            CallbackQuery callbackQuery = updateIncome.getCallbackQuery();
            chatId = callbackQuery.getMessage().getChatId();
            messageId = callbackQuery.getMessage().getMessageId();
            Integer userId = Optional.ofNullable(callbackQuery.getFrom()).map(u -> u.getId()).orElse(-1);
            System.out.println("chatId = " + chatId + ", userId = " + userId + ", name = " + callbackQuery.getFrom().getUserName());
            String buttonClicked = callbackQuery.getData();

            // process...
            new File(chatId+"").mkdir();
            data = read(chatId+"", messageId);
            data.registerNewButtonClick(userId, callbackQuery.getFrom().getUserName(), buttonClicked);

            if (callbackQuery.getMessage().getPhoto()!=null && !callbackQuery.getMessage().getPhoto().isEmpty()) {
                EditMessageMedia editMessageMedia = new EditMessageMedia()
                    .setChatId(chatId)
                    .setMessageId(messageId);
                InputMediaPhoto media = new InputMediaPhoto();
                media.setMedia(callbackQuery.getMessage().getPhoto().get(0).getFileId());
                editMessageMedia.setMedia(media);
                setButtons(data, editMessageMedia);
                executeSomething(editMessageMedia);
            } else {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setMessageId(messageId);
                editMessageText.setChatId(chatId);
                editMessageText.setText(callbackQuery.getMessage().getText());
                setButtons(data, editMessageText);
                executeSomething(editMessageText);
            }
        } else {
            throw new IgnoreException("Ignore anything different for now...");
        }
        save(chatId+"", messageId, data);
    }

    private void setButtons(MsgData data, Object event) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsList = new ArrayList<>();
        List<Map.Entry<String, Integer>> buttonsAndCount = data.getButtonsAndCount();
        for (int i = 0; i < buttonsAndCount.size(); i++) {
            Map.Entry<String, Integer> btn = buttonsAndCount.get(i);
            Integer value = btn.getValue();
            String text = btn.getKey() + " " + (value == 0 ? "" : value);
            buttonsList.add(new InlineKeyboardButton(text).setCallbackData(btn.getKey()));
        }
        buttons.add(buttonsList);
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup().setKeyboard(buttons);
        if(event instanceof EditMessageText) { //WTF?
            ((EditMessageText)event).setReplyMarkup(replyMarkup);
        } else if(event instanceof SendMessage) {
            ((SendMessage)event).setReplyMarkup(replyMarkup);
        } else if(event instanceof SendPhoto) {
            ((SendPhoto)event).setReplyMarkup(replyMarkup);
        } else if(event instanceof EditMessageMedia) {
            ((EditMessageMedia) event).setReplyMarkup(replyMarkup);
        }
    }

    private void save(String chatDir, Integer msgId, MsgData data) throws IOException {
        String dataTxt = Application.OBJECTMAPPER.writeValueAsString(data);
        Files.write(Paths.get(chatDir + "/" + msgId + ".json"),dataTxt.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private MsgData read (String chatDir, Integer messageId) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(chatDir + "/" + messageId + ".json"));
            String s = new String(bytes);
            return Application.OBJECTMAPPER.readValue(s, MsgData.class);
        } catch (FileNotFoundException | NoSuchFileException fnfe) {
            return new MsgData().init();
        }
    }

    private String requestFilePath(String field) {
        GetFile getFile = new GetFile().setFileId(field);
        try {
            return execute(getFile).getFilePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SendPhoto createSendPhoto(String chatId, File file) {
        return new SendPhoto()
                    .setChatId(chatId)
                    .setPhoto(file);
    }

    private void executeSomething(PartialBotApiMethod msg) {
        try {
            if(msg instanceof EditMessageMedia) { //TODO WTF?
                execute((EditMessageMedia)msg);
            } else if(msg instanceof EditMessageText) {
                execute((EditMessageText)msg);
            } else if(msg instanceof SendPhoto) {
                execute((SendPhoto)msg);
            } else if(msg instanceof SendMessage){
                execute((SendMessage)msg);
            } else {
                System.err.println("!!!!");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void executeSomething(SendMessage msg) {
        try {
            System.out.println("msg = " + msg);
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeSomething(BotApiMethod msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage createSendMessage(String chatId, String msg) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableNotification();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(msg);
        return sendMessage;
    }

    private void addButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("x"));
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton("Y"));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    @Override
    public String getBotUsername() {
        return "Nuttyfi";
    }

    @Override
    public String getBotToken() {
        return "675917208:AAExq8Jt51Qefj4JSnU0Zu8L3ZA0a6MGZ6E";
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}
