package dev.zymion.video.browser.app.camunda.delegates.scanShows;

import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import dev.zymion.video.browser.app.repositories.show.ShowRepository;
import dev.zymion.video.browser.app.services.ShowService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("SyncShowsWithTmdb")
public class SyncShowsWithTmdb implements JavaDelegate {


    private final ShowService showService;
    private final ShowRepository showRepository;

    public SyncShowsWithTmdb(ShowService showService, ShowRepository showRepository) {
        this.showService = showService;
        this.showRepository = showRepository;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {

        @SuppressWarnings("unchecked")
        List<Long> savedShowsIds =
                (List<Long>) execution.getVariable("savedShowsIds");

        if (savedShowsIds == null) {
            throw new IllegalStateException("savedShowsIds is missing in process variables");
        }

        List<ShowEntity> shows = showRepository.findAllById(savedShowsIds);


        // Wywołujemy serwis do usunięcia nieistniejących plików
        showService.syncShowMetadataWithTmdb(shows);
    }

}
