package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.services.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoService {

    private final Path videoFolder;
    private final ShowService showService;
    private final MediaItemRepository mediaItemRepository;
    private final MediaItemService mediaItemService;
    private final FileService fileService;

    @Autowired
    public VideoService(AppPathProperties appPathProperties, ShowService showService, MediaItemRepository mediaItemRepository, MediaItemService mediaItemService, AppPathProperties appPathProperties1, FileService fileService) {
        this.showService = showService;
        this.mediaItemRepository = mediaItemRepository;
        this.mediaItemService = mediaItemService;
        this.videoFolder = appPathProperties.getVideoFolder();
        this.fileService = fileService;
    }

    //ToDO
    //skrypt to konwersji napisow srt na vtt
//    ffmpeg -sub_charenc windows-1250 -i napisy.srt napisy.vtt

    //ToDO skrypt do zmiany formatu audio na obslugiwany
//    ffmpeg -i input.mkv -c:v copy -c:a aac -b:a 192k output.mkv

    //ToDO polecenie do robienie zdjec co 10 min przez pierwsze 30 min
//    ffmpeg -i Everest.mp4 -vf "fps=1/300" -t 1800 -q:v 2 output_%03d.jpg
    //ffmpeg -i "Demon slayer - Infinity train.mp4" -t 00:30:00 -vf "fps=1/600" -qscale:v 2 thumbnails/thumb_%03d.jpg

    public void scanAllVideos() {
        List<Path> files = fileService.findAllVideoFiles(videoFolder);

        // 1️⃣ Zbieramy wszystkie aktualne relativePath-y
        Set<String> currentRelativePaths = files.stream()
                .map(path -> videoFolder.relativize(path).toString().replace("\\", "/"))
                .collect(Collectors.toSet());

        // 2️⃣ Przetwarzamy nowe/zmienione pliki
        List<MediaItemEntity> mediaItems = files.stream()
                .map(mediaItemService::buildEntityFromPath)
                .filter(Objects::nonNull)
                .toList();


        //tutaj beda tylko te ktore sie zmienily lub nowe
        List<MediaItemEntity> savedMediaItems = mediaItemRepository.saveAll(mediaItems);

        // 3️⃣ Usuwamy z bazy te, których już nie ma na dysku
        mediaItemService.removeNotExistingMediaItems(currentRelativePaths);

        List<ShowEntity> savedShows = showService.setUpShows(savedMediaItems);


        //ustawianie struktur shows
        showService.setUpShowsStructureType();

        //fetch api tmdb data
        showService.syncShowMetadataWithTmdb(savedShows);

    }








}
