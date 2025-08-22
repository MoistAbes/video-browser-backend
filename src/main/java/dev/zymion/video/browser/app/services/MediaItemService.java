package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaItemService {

    private final MediaItemRepository mediaItemRepository;

    public MediaItemService(MediaItemRepository mediaItemRepository) {
        this.mediaItemRepository = mediaItemRepository;
    }

    public List<MediaItemEntity> saveAll(List<MediaItemEntity> mediaItems) {
        return mediaItemRepository.saveAll(mediaItems);
    }

}
