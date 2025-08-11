package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.repositories.EpisodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    @Autowired
    public EpisodeService(EpisodeRepository episodeRepository) {
        this.episodeRepository = episodeRepository;
    }
}
