import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/*
* Bot commands list:
* greetings - greetings the user as a true gangsta
* pic - send me a picture
* pics_kb - show pictures keyboard
* hide_kb - hide all keyboards
* */

public class MyTestBot extends TelegramLongPollingBot{

    private static final String BOT_USERNAME = "@MyTest248Bot";
    private static final String BOT_TOKEN = "441814070:AAEmwPD8-eISLwGwkExcYPajmzBuFg94IDY";

    private static final String COMMAND_GREETINGS = "/greetings";
    private static final String COMMAND_PIC = "/pic";
    private static final String COMMAND_PICS_KB = "/pics_kb";
    private static final String COMMAND_HIDE_KB = "/hide_kb";

    private static final String MESSAGE_NO_PICS = "I have no pictures yet. U can download them homie";
    private static final String MESSAGE_UNKNOWN_COMMAND = "I dont know this command sorry man";
    private static final String MESSAGE_SHOW_PICS_KEYBOARD = "Here is your keyboard bro";
    private static final String MESSAGE_HIDE_KB = "Keyboard hidden (like a ninja)";
    private static final String MESSAGE_BAD_REQUEST = "R u kidding me mafaker?";

    private List<String> photoIds = new ArrayList<>();
    private boolean isKeyboardShown = false;

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText() && !update.getMessage().isCommand()) {
            if (update.getMessage().getText().contains("Picture") && isKeyboardShown) {
                botSendKbPic(update);
            }
            else botCopyMessage(update);
        }

        else if (update.hasMessage() && update.getMessage().isCommand()) {
            String messageText = update.getMessage().getText();
            switch (messageText) {
                case COMMAND_GREETINGS:
                    botSendGreetings(update);
                    break;
                case COMMAND_PIC:
                    botSendRandomPic(update);
                    break;
                case COMMAND_PICS_KB:
                    botShowKeyboard(update);
                    break;
                case COMMAND_HIDE_KB:
                    botHideKeyboards(update);
                    break;
                default:
                    botUnknownCommand(update);
                    break;
            }
        }

        else if (update.hasMessage() && !update.getMessage().isCommand() && update.getMessage().hasPhoto()) {
            //Only if message is not a command, contains one photo and caption
            System.out.println("Uploading Photo");
            botUploadPhoto(update);
        }

    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    private void botCopyMessage(Update update) {
        String msgText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        SendMessage msg = new SendMessage().setChatId(chatId).setText(msgText);
        try {
            sendMessage(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void botSendGreetings(Update update) {
        String user = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
        String msgText = EmojiParser.parseToUnicode("Hey, " + user + " homie! :pizza:");//"Hey, " + user + " homie!";
        long chatId = update.getMessage().getChatId();
        SendMessage msg = new SendMessage().setChatId(chatId).setText(msgText);
        try {
            sendMessage(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void botUploadPhoto(Update update) {
        long chatId = update.getMessage().getChatId();
        List<PhotoSize> photos = update.getMessage().getPhoto();
        String photoId = photos.stream().findAny().orElse(null).getFileId();
        if (!photoIds.contains(photoId)) photoIds.add(photoId);
        int photoW = photos.stream().findAny().orElse(null).getWidth();
        int photoH = photos.stream().findAny().orElse(null).getHeight();
        String caption = "Caption: " + update.getMessage().getCaption() + "\nid: " + photoId + "\nwidth: " + photoW +
                "\nheight: " + photoH;
        SendPhoto msg = new SendPhoto().setChatId(chatId).setPhoto(photoId).setCaption(caption);
        try {
            sendPhoto(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void botSendRandomPic(Update update) {
        long chatId = update.getMessage().getChatId();
        if (photoIds.size() > 0) {
            int picNum = (int) (Math.random()*photoIds.size());
            String picId = photoIds.get(picNum);
            String caption = "Ok bro, u got picture " + (picNum+1) + "/" + photoIds.size();
            SendPhoto msg = new SendPhoto().setChatId(chatId).setPhoto(picId).setCaption(caption);
            try {
                sendPhoto(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            try {
                sendMessage(new SendMessage(chatId, MESSAGE_NO_PICS));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void botShowKeyboard(Update update) {
        long chatId = update.getMessage().getChatId();
        if (photoIds.size() > 0) {
            SendMessage message = new SendMessage(chatId, MESSAGE_SHOW_PICS_KEYBOARD);
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            for (String s: photoIds) {
                KeyboardRow row = new KeyboardRow();
                row.add(new KeyboardButton("Picture " + (photoIds.indexOf(s) + 1)));
                keyboard.add(row);
            }
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(COMMAND_HIDE_KB));
            keyboard.add(row);
            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);
            try {
                sendMessage(message);
                isKeyboardShown = true;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            SendMessage message = new SendMessage(chatId,MESSAGE_NO_PICS);
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void botSendKbPic(Update update) {
        boolean incorrect = false;
        long chatId = update.getMessage().getChatId();
        String[] parts = update.getMessage().getText().split(" ");
        if (parts.length == 2) {
            try {
                int picNum = Integer.parseInt(parts[1]) - 1;
                if (picNum >= 0 && picNum < photoIds.size()) {
                    SendPhoto msg = new SendPhoto().setChatId(chatId).setPhoto(photoIds.get(picNum));
                    try {
                        sendPhoto(msg);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    incorrect = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                incorrect = true;
            }
        } else {
            incorrect = true;
        }
        if (incorrect) {
            SendMessage msg = new SendMessage(chatId, MESSAGE_BAD_REQUEST);
            try {
                sendMessage(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void botHideKeyboards(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage msg = new SendMessage(chatId, MESSAGE_HIDE_KB).setReplyMarkup(new ReplyKeyboardRemove());
        try {
            sendMessage(msg);
            isKeyboardShown = false;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void botUnknownCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage msg = new SendMessage(chatId, MESSAGE_UNKNOWN_COMMAND);
        try {
            sendMessage(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
