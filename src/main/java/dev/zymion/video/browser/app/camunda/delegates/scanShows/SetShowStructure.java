package dev.zymion.video.browser.app.camunda.delegates.scanShows;

import dev.zymion.video.browser.app.repositories.show.ShowRepository;
import dev.zymion.video.browser.app.services.ShowService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("setShowsStructureDelegate")
public class SetShowStructure implements JavaDelegate {

    private final ShowRepository showRepository;
    private final ShowService showService;

    public SetShowStructure(ShowRepository showRepository, ShowService showService) {
        this.showRepository = showRepository;
        this.showService = showService;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {

        @SuppressWarnings("unchecked")
        List<Long> savedShowsIds =
                (List<Long>) execution.getVariable("savedShowsIds");

        if (savedShowsIds == null) {
            throw new IllegalStateException("savedShowsIds is missing in process variables");
        }


        // Wywołujemy serwis do usunięcia nieistniejących plików
        showService.setUpShowsStructureType(savedShowsIds);
    }
}
