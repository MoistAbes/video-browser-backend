package dev.zymion.video.browser.app.camunda.delegates.scanShows;

import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.services.ShowService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("setUpShowsDelegate")
public class SetUpShowsDelegate implements JavaDelegate {

    private final ShowService showService;
    private final MediaItemRepository mediaItemRepository;

    public SetUpShowsDelegate(ShowService showService, MediaItemRepository mediaItemRepository) {
        this.showService = showService;
        this.mediaItemRepository = mediaItemRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        @SuppressWarnings("unchecked")
        List<Long> savedMediaItemIds =
                (List<Long>) execution.getVariable("savedMediaItemIds");


        if (savedMediaItemIds == null) {
            throw new IllegalStateException("savedMediaItemIds is missing in process variables");
        }

        List<MediaItemEntity> mediaItems = mediaItemRepository.findAllById(savedMediaItemIds);

        // Wywołujemy serwis do usunięcia nieistniejących plików
        List<Long> savedShowsIds = showService.setUpShows(mediaItems).stream()
                .map(ShowEntity::getId).toList();

        execution.setVariable("savedShowsIds", savedShowsIds);
    }

}
