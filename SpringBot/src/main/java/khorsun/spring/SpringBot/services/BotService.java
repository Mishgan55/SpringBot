package khorsun.spring.SpringBot.services;

import com.vdurmont.emoji.EmojiParser;
import khorsun.spring.SpringBot.config.BotConfig;
import khorsun.spring.SpringBot.models.User;
import khorsun.spring.SpringBot.util.UserValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UserService userService;
    static final String YES_BUTTON="YES_BUTTON";
    static final String NO_BUTTON="NO_BUTTON";
    static final String HELP_COMMAND="Bot under development.\n\n" +
            "You can only use these commands:\n\n" +
            "/start - Sends a welcome message.\n\n" +
            "/createdBy - Command shows the creator of the bot.\n\n"+
            "/delete = Command for deleting your account";
    @Autowired
    public BotService( BotConfig botConfig, UserService userService) {
        this.botConfig = botConfig;
        this.userService = userService;
    }


    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            /*With the help of the /send command,
             we can make a newsletter inside telegrams for all users that we have in the database*/
            if (text.contains("/send")&&chatId.equals(botConfig.getBotOwner())){
                var textToSend = EmojiParser.parseToUnicode(text.substring(text.indexOf(" ")));
                var allUsers = userService.findAllUsers();

                for (User allUser : allUsers) {
                    sendMessage(allUser.getId(),textToSend);
                }
            }else {
                switch (text) {
                    case "/start":
                        userService.saveUser(update.getMessage());
                        sendWelcomeMessage(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/createdby":
                        sendInformationAboutCreatedPerson(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/delete":
                        delete(chatId);
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_COMMAND);
                        break;
                    default:
                        sendMessage(chatId, "Sorry, this command doesn't work");
                }
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageID = update.getCallbackQuery().getMessage().getMessageId();

            if (data.equals(NO_BUTTON)) {
                String text = "Thank You";
                executeEditMessageText(chatId,text,messageID);
            } else if (data.equals(YES_BUTTON)) {
                String text = "You successfully delete your account";
                executeEditMessageText(chatId,text,messageID);
                userService.delete(chatId);
            }

        }
    }

    private void delete(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Do you really want to delete your account?");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboards=new ArrayList<>();

        List<InlineKeyboardButton> keyBoard = new ArrayList<>();

        var yesButton= new InlineKeyboardButton();


        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton=new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        keyBoard.add(yesButton);
        keyBoard.add(noButton);

        keyboards.add(keyBoard);
        inlineKeyboardMarkup.setKeyboard(keyboards);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        executeMessage(sendMessage);

    }

    private void sendWelcomeMessage(Long chatId, String name){

        String welcomeWords= EmojiParser.parseToUnicode("Hello, " + name + ", nice to meet you!"+":blush:");
        log.info("Send message to user: "+name);
        sendMessage(chatId,welcomeWords);

    }
    private void sendInformationAboutCreatedPerson(Long chatId,String name){
        String createdMessage="This bot created by @MikhailKhorsun";
        log.info("Send message to user: "+name);
        sendMessage(chatId,createdMessage);
    }

    private void sendMessage(Long chatId, String message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        executeMessage(sendMessage);
    }
    private void executeEditMessageText(Long chatId, String text, Integer messageId){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Editing error occurred: "+e.getMessage());
        }
    }
    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: "+e.getMessage());
        }
    }
}
