package khorsun.spring.SpringBot.services;

import khorsun.spring.SpringBot.models.User;
import khorsun.spring.SpringBot.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findOne(Long id){
        return userRepository.findById(id);

    }

    public void saveUser(Message message){
        if (findOne(message.getChatId()).isEmpty()){
           var chatId= message.getChatId();
           var chat=message.getChat();

            User user = new User();

            user.setId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("User with id: " +chatId+" saved in db");

        }

    }
}
