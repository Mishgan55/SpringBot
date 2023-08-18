package khorsun.spring.SpringBot.services;

import khorsun.spring.SpringBot.models.Ad;
import khorsun.spring.SpringBot.repositories.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdService {

    private final AdRepository adRepository;
    @Autowired
    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    public Ad findAd(int id){
        return adRepository.findById(id).orElse(null);
    }
}
