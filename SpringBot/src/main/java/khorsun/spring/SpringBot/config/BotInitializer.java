package khorsun.spring.SpringBot.config;

import khorsun.spring.SpringBot.services.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final BotService botService;
    @Autowired
    public BotInitializer(BotService botService) {
        this.botService = botService;
    }
    @EventListener({ContextRefreshedEvent.class})
    public void connect() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

        telegramBotsApi.registerBot(botService);
    }
}
