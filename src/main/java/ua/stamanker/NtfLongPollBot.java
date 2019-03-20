package ua.stamanker;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ua.stamanker.Emoji.THUMB_DN;
import static ua.stamanker.Emoji.THUMB_UP;

public class NtfLongPollBot extends TelegramLongPollingBot {

    public static final long botPrivatechatId = 31540560L;

    @Override
    public void onUpdateReceived(Update updateIncome) {
        try {
            processUpdateReceived(updateIncome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processUpdateReceived(Update updateIncome) throws Exception {
        if(updateIncome.hasMessage()) {
            Message message1 = updateIncome.getMessage();
            String message = message1.getText();
            if(message1.getChatId() != botPrivatechatId) {
                System.out.println("# ignore other chats messages: " + message1.getChatId());
                return;
            } else {
                System.out.println("chatId = " + message1.getChatId());
            }
            sendMsg(message1.getChatId().toString(), message);
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
                //sendPhoto(message1.getChatId()+"", TgFile.getFileUrl(getBotToken(), filePath));
                sendPhoto(message1.getChatId() + "", f);
                if (!f.delete()) {
                    System.out.println("File not deleted: " + f.getAbsolutePath());
                }
            }
        } else if (updateIncome.hasCallbackQuery()) {
            //downloadFile("https://api.telegram.org/bot"+getBotToken()+"/"+updateIncome.getFilePath());

            CallbackQuery callbackQuery = updateIncome.getCallbackQuery();
            Long chatId = callbackQuery.getMessage().getChatId();
            Integer messageId = callbackQuery.getMessage().getMessageId();

            final String chatDir = chatId + "";
            final String msgDir = chatId + "/" + messageId;
            String userId = msgDir + "/" + callbackQuery.getMessage().getFrom().getId();
            new File(chatDir).mkdir();
            new File(msgDir).mkdir();
            File userIdFile = new File(userId);
            if(userIdFile.exists()) {
                System.out.println("already voted");
                return;
            } else {
                createAndWrite2UserIdFile(userIdFile, Utils.getBefore(callbackQuery.getData(), "-"));
            }

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            answer.setText("response to callback " + callbackQuery.getId());
            //MessageEntity messageEntity = callbackQuery.getMessage().getEntities().get(0);

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setMessageId(messageId);
            editMessageText.setChatId(chatId);
            editMessageText.setText(callbackQuery.getMessage().getText());

            System.out.println("answer = " + answer.getText());

            int a = 0;
            int b = 0;
            Integer num = Utils.getNum(callbackQuery.getData(), "-");
            if(callbackQuery.getData().startsWith("A")) {
                a = ++num;
            } else {
                b = ++num;
            }

            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttonsList = new ArrayList<>();
            buttonsList.add(new InlineKeyboardButton(THUMB_UP + a).setCallbackData("A-"+a));
            buttonsList.add(new InlineKeyboardButton(THUMB_DN + b).setCallbackData("B-"+b));
            buttons.add(buttonsList);

            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            // ---
            editMessageText.setReplyMarkup(markupKeyboard);
            executeSomething(editMessageText);
        }
    }

    private void createAndWrite2UserIdFile(File userIdFile, String answer) throws IOException {
        //userIdFile.createNewFile();
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(userIdFile))) {
            bufferedWriter.write(answer);
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

    private void sendPhoto(String chatId, String url) {
        System.out.println("url = " + url);
        SendPhoto sendPhoto = new SendPhoto()
                .setChatId(chatId)
                .setPhoto(url);
        executeSomething(sendPhoto);
    }

    private void sendPhoto(String chatId, File file) {
        System.out.println("send file = " + file.getAbsolutePath());
        SendPhoto sendPhoto = new SendPhoto()
                .setChatId(chatId)
                .setPhoto(file);
        executeSomething(sendPhoto);
    }

    private void executeSomething(SendPhoto msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void executeSomething(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void executeSomething(BotApiMethod msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @param chatId id чата
     * @param msg
     */
    public synchronized void sendMsg(String chatId, String msg) {
        System.out.println("chatId = " + chatId + ", msg = " + msg);
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableNotification();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText("<b>"+msg+"</b>");
        setInline(sendMessage);
        executeSomething(sendMessage);
    }

    //    private void executeSomething(BotApiMethod m) {
//        try {
//            execute(m);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }
//
    private void setInline(SendMessage sendMessage) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        buttons1.add(new InlineKeyboardButton().setText(THUMB_UP).setCallbackData("A-0"));
        buttons1.add(new InlineKeyboardButton().setText(THUMB_DN).setCallbackData("B-0"));
        buttons.add(buttons1);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        // ---
        sendMessage.setReplyMarkup(markupKeyboard);
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
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}
