package khorsun.spring.SpringBot.util;

import khorsun.spring.SpringBot.models.User;
import khorsun.spring.SpringBot.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.telegram.telegrambots.meta.api.objects.Message;
@Component
@Slf4j
public class UserValidation implements Validator {
    private final UserService userService;

    public UserValidation(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(User.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Message message=(Message) target;

        if (userService.findOne(message.getChatId()).isPresent()){
            log.error("This user already register"+errors.getObjectName());
        }

    }
}
