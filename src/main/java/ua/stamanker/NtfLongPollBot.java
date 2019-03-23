package ua.stamanker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.facilities.TelegramHttpClientBuilder;
import org.telegram.telegrambots.meta.ApiContext;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import ua.stamanker.entities.MsgData;
import ua.stamanker.entities.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.telegram.telegrambots.Constants.SOCKET_TIMEOUT;

public class NtfLongPollBot extends TelegramLongPollingBot {

    public static final String COMMAND_2CHAT = "/chat:";
    public static final String SET_BUTTONS = "/set buttons:";
    public static final String SET_BUTTONS_DFLT = "/set buttons default:";
    private final Settings settings;
    private final Chats chats;
    private final FileWorker fileWorker;

    public NtfLongPollBot(Chats chats, FileWorker fileWorker, Settings settings) {
        this.chats = chats;
        this.fileWorker = fileWorker;
        this.settings = settings;
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
                    }
                }
        );
    }

    public synchronized void processUpdateReceived(Update updateIncome) throws Exception {
        System.out.println(" ------------------------ " + new Date());
        MsgData data;
        Integer messageId;
        Long chatId;
        Integer userId;
        if (updateIncome.hasMessage()) {
            Message message1 = updateIncome.getMessage();
            String msgText = message1.getText();
            //messageId = message1.getMessageId();
            userId = message1.getFrom().getId();
            chatId = message1.getChatId();
            if (msgText != null && msgText.startsWith("/")) {
                System.out.println(msgText);
                String responseMsg;
                if (msgText.startsWith(COMMAND_2CHAT)) {
                    long chat2Store = Long.parseLong(Utils.getAfter(msgText, COMMAND_2CHAT).trim());
                    chats.store2Post(userId, chat2Store, null);
                    responseMsg = "Accepted: will send next message to chat " + chat2Store + "/" + chats.getChatNameById(chat2Store);
                } else if (msgText.startsWith(SET_BUTTONS)) {
                    String smilesRaw = Utils.getAfter(msgText, SET_BUTTONS);
                    List<String> buttons = getButtonsFromText(smilesRaw);
                    chats.store2Post(userId, null, buttons);
                    responseMsg = "Set buttons for next post from " + message1.getFrom() + "/" + message1.getFrom().getUserName() + ": " + buttons + ";" + chats.getChat2RePost(userId);
                } else if (msgText.startsWith(SET_BUTTONS_DFLT)) {
                    String smilesRaw = Utils.getAfter(msgText, SET_BUTTONS_DFLT);
                    List<String> buttons = getButtonsFromText(smilesRaw);
                    chats.storeDefaultButtons(userId, null, buttons);
                    responseMsg = "x";
                } else {
                    responseMsg = "Command not recognized: " + msgText;
                }

                executeSomething(createSendMessage(chatId, responseMsg));
                return;
            }
            System.out.println("chatId = " + chatId + " / " + chats.getChatNameById(chatId));
//            if (message1.getChatId() != chats.getPrivateChatId()) {
//                throw new IgnoreException("# ignore other chat: " + chatId + " / " + chats.getChatNameById(chatId));
//            }
            // -------------------------------------------------------- IT'S TIME 2 REFACTOR
            Chats.X chat2Post = chats.getChat2RePostAndRemove(userId);
            data = new MsgData().initButtons(chat2Post.buttons).setChatId2Store(chat2Post.chatId);
            if (message1.hasPhoto()) {
                int num = message1.getPhoto().size();
                String filePath;
                if (!message1.getPhoto().get(num - 1).hasFilePath()) {
                    filePath = requestFilePath(message1.getPhoto().get(num - 1).getFileId());
                } else {
                    filePath = message1.getPhoto().get(num - 1).getFilePath();
                }
                File f = downloadFile(filePath);
                System.out.println("\tfile = " + f.getAbsolutePath());
                SendPhoto sendPhoto = createSendPhoto(chat2Post.chatId, f);
                setButtons(data, sendPhoto);
                Message executeRspns = execute(sendPhoto);
                fileWorker.save(data.chatId2Store, executeRspns.getMessageId(), data);
                if (!f.delete()) {
                    System.out.println("\tFile not deleted: " + f.getAbsolutePath());
                }
            } else {
                SendMessage sendMessage = createSendMessage(chat2Post.chatId, msgText);
                setButtons(data, sendMessage);
                Message executeRspns = execute(sendMessage);
                System.out.println("\tresponse = " + executeRspns);
                fileWorker.save(data.chatId2Store, executeRspns.getMessageId(), data);
            }
        } else if (updateIncome.hasCallbackQuery()) {
            CallbackQuery callbackQuery = updateIncome.getCallbackQuery();
            chatId = callbackQuery.getMessage().getChatId();
            messageId = callbackQuery.getMessage().getMessageId();
            User from = callbackQuery.getFrom();
            userId = Optional.ofNullable(from).map(User::getId).orElse(-1);
            System.out.println("\tchatId = " + chatId + ", msgId = " + messageId + " | userId = " + userId + ", name = " + from.getUserName() + ", " + from.getFirstName() + " " + from.getLastName());
            String buttonClicked = callbackQuery.getData();

            // process...
            data = Optional.ofNullable(fileWorker.read(chatId, messageId).setChatId2Store(chatId)).orElse(new MsgData().initDefault());
            data.registerNewButtonClick(userId, from.getUserName(), buttonClicked);

            if (callbackQuery.getMessage().getPhoto() != null && !callbackQuery.getMessage().getPhoto().isEmpty()) {
                EditMessageMedia editMessageMedia = new EditMessageMedia()
                        .setChatId(chatId)
                        .setMessageId(messageId);
                InputMediaPhoto media = new InputMediaPhoto();
                media.setMedia(callbackQuery.getMessage().getPhoto().get(0).getFileId());
                editMessageMedia.setMedia(media);
                setButtons(data, editMessageMedia);
                execute(editMessageMedia);
            } else {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setMessageId(messageId);
                editMessageText.setChatId(chatId);
                editMessageText.setText(callbackQuery.getMessage().getText());
                setButtons(data, editMessageText);
                execute(editMessageText);
            }
            fileWorker.save(data.chatId2Store, messageId, data);
        } else {
            throw new IgnoreException("*** Ignore anything different for now...");
        }
    }

    private List<String> getButtonsFromText(String smilesRaw) {
        System.out.println("\tsmilesRaw = '" + smilesRaw + "'");
        List<String> buttons = new ArrayList<>();
        String[] s = smilesRaw.split(" ");
        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].trim();
            if (!s[i].equals(" ") && !s[i].isEmpty()) {
                buttons.add(s[i]);
            }
        }
        System.out.println("\tbuttons = " + buttons);
        return buttons;
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
        if (event instanceof EditMessageText) { //WTF?
            ((EditMessageText) event).setReplyMarkup(replyMarkup);
        } else if (event instanceof SendMessage) {
            ((SendMessage) event).setReplyMarkup(replyMarkup);
        } else if (event instanceof SendPhoto) {
            ((SendPhoto) event).setReplyMarkup(replyMarkup);
        } else if (event instanceof EditMessageMedia) {
            ((EditMessageMedia) event).setReplyMarkup(replyMarkup);
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

    private SendPhoto createSendPhoto(long chatId, File file) {
        return new SendPhoto()
                .setChatId(chatId)
                .setPhoto(file);
    }

//    private void executeSomething(PartialBotApiMethod msg) {
//        executeSomethingAsync(msg, new SentCallback() {
//            @Override
//            public void onResult(BotApiMethod method, Serializable response) {
//
//            }
//
//            @Override
//            public void onError(BotApiMethod method, TelegramApiRequestException apiException) {
//
//            }
//
//            @Override
//            public void onException(BotApiMethod method, Exception exception) {
//
//            }
//        });
//    }
//
//    private void executeSomethingAsync(PartialBotApiMethod msg, SentCallback callback) {
//        try {
//            if(msg instanceof EditMessageMedia) { //TODO WTF?
//                executeAsync(msg, callback);
//            } else if(msg instanceof EditMessageText) {
//                executeAsync(msg, callback);
//            } else if(msg instanceof SendPhoto) {
//                executeAsync(msg, callback);
//            } else if(msg instanceof SendMessage){
//                executeAsync(msg, callback);
//            } else {
//                System.err.println("!!!!");
//            }
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }

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

    private SendMessage createSendMessage(long chatId, String msg) {
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
        return settings.botUsername;
    }

    @Override
    public String getBotToken() {
        return settings.botToken;
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}
