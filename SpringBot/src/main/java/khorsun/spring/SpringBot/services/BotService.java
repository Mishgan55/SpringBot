package khorsun.spring.SpringBot.services;

import com.vdurmont.emoji.EmojiParser;
import khorsun.spring.SpringBot.config.BotConfig;
import khorsun.spring.SpringBot.util.UserValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
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


    private final UserValidation userValidation;
    private final BotConfig botConfig;
    private final UserService userService;
    @Autowired
    public BotService(UserValidation userValidation, BotConfig botConfig, UserService userService) {
        this.userValidation = userValidation;

        this.botConfig = botConfig;
        this.userService = userService;
        List<BotCommand> listOfCommands=new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","Sends a welcome message"));
        listOfCommands.add(new BotCommand("/createdby","Command shows the creator of the bot"));
        listOfCommands.add(new BotCommand("/help","Bot information"));

        try {
            this.execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(),null));
        } catch (TelegramApiException e) {
            log.error("Error menu information: "+e.getMessage());
        }
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
                    sendMessage(chatId, "Bot under development.\n\n" +
                            "You can only use these commands:\n\n" +
                            "/start - Sends a welcome message.\n\n" +
                            "/createdBy - Command shows the creator of the bot.");
                    break;
                default:
                    sendMessage(chatId, "Sorry, this command doesn't work");
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer message = update.getCallbackQuery().getMessage().getMessageId();

            if (data.equals("NO_BUTTON")) {
                String text = "Thank You";
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId(message);
                editMessageText.setText(text);
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: "+e.getMessage());
                }

            } else if (data.equals("YES_BUTTON")) {
                String text = "You successfully delete your account";
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId(message);
                editMessageText.setText(text);
                userService.delete(chatId);
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: "+e.getMessage());
                }
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
        yesButton.setCallbackData("YES_BUTTON");

        var noButton=new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData("NO_BUTTON");

        keyBoard.add(yesButton);
        keyBoard.add(noButton);

        keyboards.add(keyBoard);
        inlineKeyboardMarkup.setKeyboard(keyboards);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: "+e.getMessage());
        }

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

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows= new ArrayList<>();

        KeyboardRow row= new KeyboardRow();

        row.add("help");
        row.add("instruction");

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: "+e.getMessage());
        }
    }
}
