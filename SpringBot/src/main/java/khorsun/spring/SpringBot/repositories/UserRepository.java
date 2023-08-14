package khorsun.spring.SpringBot.repositories;

import khorsun.spring.SpringBot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
}
