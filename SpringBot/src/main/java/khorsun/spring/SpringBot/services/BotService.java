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

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
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

        if (update.hasMessage()&&update.getMessage().hasText()){
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            switch (text){
                case "/start" :
                    userService.saveUser(update.getMessage());
                    sendWelcomeMessage(chatId,update.getMessage().getChat().getFirstName());
                break;
                case "/createdby" :
                    sendInformationAboutCreatedPerson(chatId,update.getMessage().getChat().getFirstName());
                break;
                case "/help" :
                    sendMessage(chatId,"Bot under development.\n\n"+
                            "You can only use these commands:\n\n"+
                            "/start - Sends a welcome message.\n\n"+
                            "/createdBy - Command shows the creator of the bot.");
                    break;
                default: sendMessage(chatId, "Sorry, this command doesn't work");
            }
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

        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: "+e.getMessage());
        }
    }
}
