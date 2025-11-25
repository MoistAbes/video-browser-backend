package dev.zymion.video.browser.app.camunda.delegates.scanShows;

import dev.zymion.video.browser.app.services.MediaItemService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component("removeOldMediaItemsDelegate")
public class RemoveOldMediaItemsDelegate implements JavaDelegate {

    private final MediaItemService mediaItemService;

    public RemoveOldMediaItemsDelegate(MediaItemService mediaItemService) {
        this.mediaItemService = mediaItemService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        @SuppressWarnings("unchecked")
        Set<String> currentRelativePaths =
                (Set<String>) execution.getVariable("currentRelativePaths");

        if (currentRelativePaths == null) {
            throw new IllegalStateException("currentRelativePaths is missing in process variables");
        }

        // Wywołujemy serwis do usunięcia nieistniejących plików
        mediaItemService.removeNotExistingMediaItems(currentRelativePaths);

        execution.setVariable("removedMediaCount", currentRelativePaths.size());
    }

}
