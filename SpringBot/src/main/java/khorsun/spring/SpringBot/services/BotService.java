package khorsun.spring.SpringBot.services;

import khorsun.spring.SpringBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    public BotService(BotConfig botConfig) {
        this.botConfig = botConfig;
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
                case "/start" : sendWelcomeMessage(chatId,update.getMessage().getChat().getFirstName());
                break;
                case "/createdBy" : sendInformationAboutCreatedPerson(chatId);
                break;
                default: setMessage(chatId, "Sorry, this command doesn't work");
            }
        }

    }

    private void sendWelcomeMessage(Long chatId, String name){

        String welcomeWords="Hello, " + name + ", nice to meet you!";
        log.info("Send message to user: "+name);
        setMessage(chatId,welcomeWords);

    }
    private void sendInformationAboutCreatedPerson(Long chatId){
        String createdMessage="This bot created by @MikhailKhorsun";
        setMessage(chatId,createdMessage);
    }

    private void setMessage(Long chatId, String message){
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
