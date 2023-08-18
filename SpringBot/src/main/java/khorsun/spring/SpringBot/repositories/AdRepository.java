package khorsun.spring.SpringBot.repositories;

import khorsun.spring.SpringBot.models.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdRepository extends JpaRepository<Ad,Integer> {
}
