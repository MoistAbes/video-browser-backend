package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.repositories.show.SeasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;

    @Autowired
    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }
}
