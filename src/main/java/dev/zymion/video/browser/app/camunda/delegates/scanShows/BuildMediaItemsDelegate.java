package dev.zymion.video.browser.app.camunda.delegates.scanShows;

import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.services.MediaItemService;
import dev.zymion.video.browser.app.services.util.VideoFileUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component("buildMediaItemsDelegate")
public class BuildMediaItemsDelegate implements JavaDelegate {

    private final MediaItemService mediaItemService;
    private final MediaItemRepository mediaItemRepository;
    private final VideoFileUtils videoFileUtils;

    public BuildMediaItemsDelegate(MediaItemService mediaItemService,
                                   MediaItemRepository mediaItemRepository,
                                   VideoFileUtils videoFileUtils) {
        this.mediaItemService = mediaItemService;
        this.mediaItemRepository = mediaItemRepository;
        this.videoFileUtils = videoFileUtils;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String videoFolder = (String) execution.getVariable("videoFolder");
        @SuppressWarnings("unchecked")
        List<String> files = (List<String>) execution.getVariable("foundFiles");

        // zamiana List<String> na Set<Path>
        Set<Path> absolutePaths = files.stream()
                .map(Path::of)
                .collect(Collectors.toSet());

        // wywołanie utils z Path
        Set<String> currentRelativePaths = videoFileUtils.toRelativePaths(
                absolutePaths,
                Path.of(videoFolder)
        );


        List<MediaItemEntity> mediaItems = files.stream()
                .map(Path::of) // konwertujemy String -> Path
                .map(mediaItemService::buildEntityFromPath)
                .filter(Objects::nonNull)
                .toList();

        List<MediaItemEntity> savedMediaItems = mediaItemRepository.saveAll(mediaItems);

        List<Long> savedMediaItemIds = savedMediaItems.stream()
                .map(MediaItemEntity::getId)
                .toList();

        execution.setVariable("savedMediaItemIds", savedMediaItemIds);
        execution.setVariable("currentRelativePaths", currentRelativePaths);

    }
}
