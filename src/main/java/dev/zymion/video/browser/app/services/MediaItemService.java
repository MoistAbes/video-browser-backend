package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.services.file.FileService;
import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MediaItemService {

    private final MediaItemRepository mediaItemRepository;
    private final Path videoFolder;
    private final FileService fileService;

    public MediaItemService(MediaItemRepository mediaItemRepository, AppPathProperties appPathProperties, FileService fileService) {
        this.mediaItemRepository = mediaItemRepository;
        this.videoFolder = appPathProperties.getVideoFolder();
        this.fileService = fileService;
    }


    public MediaItemEntity buildEntityFromPath(Path path)   {
        String fileName = path.getFileName().toString();
        String title = fileName.replaceFirst("\\.[^.]+$", "");

        // Ścieżka względem folderu głównego np. ANIME/ReZero/Season 2/02.mp4
        String relativePath = videoFolder.relativize(path).toString().replace("\\", "/");
        String rootPath = relativePath.substring(0, relativePath.lastIndexOf("/"));
        String[] parts = relativePath.split("/");

        String fileNameWithExtension = parts[parts.length - 1];

        MediaTypeEnum mediaTypeEnum;
        String parentTitle;
        Integer season = null;
        Integer episode = null;

        mediaTypeEnum = MediaTypeEnum.MOVIE;
        parentTitle = parts[1];

        // Parsujemy sezon z folderu, np. Season1 → 1
        Matcher seasonMatcher = Pattern.compile("Season\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(parts[2]);
        // Parsujemy odcinek z nazwy pliku, np. S01E03
        Matcher epMatcher = Pattern.compile("S\\d{1,2}E(\\d{1,2})", Pattern.CASE_INSENSITIVE).matcher(title);
        if (seasonMatcher.find() && epMatcher.find()) {
            season = Integer.parseInt(seasonMatcher.group(1));
            episode = Integer.parseInt(epMatcher.group(1));
            mediaTypeEnum = MediaTypeEnum.TV;
        }

//        System.out.println(title + " | " + relativePath + " | " + parentTitle + " | " + season + " | " + episode);
        String resultHash = fileService.computeMetadataHash(path);

        Optional<MediaItemEntity> optionalMediaItemEntity =
                mediaItemRepository.findByVideoHash(resultHash);



        if (optionalMediaItemEntity.isPresent()) {
            MediaItemEntity existing = optionalMediaItemEntity.get();

            if (!existing.getVideoHash().equals(resultHash)) {
                log.info("File modified: " + rootPath + " " + title);
                // zwróć zaktualizowany obiekt
                return MediaItemEntity.builder()
                        .id(existing.getId())
                        .title(title)
                        .rootPath(rootPath)
                        .fileName(fileNameWithExtension)
                        .parentTitle(parentTitle)
                        .seasonNumber(season)
                        .episodeNumber(episode)
                        .codec(existing.getCodec().orElse(null))             // było w VideoTechnicalDetailsEntity
                        .audio(existing.getAudio().orElse(null))             // było w VideoTechnicalDetailsEntity
                        .duration(existing.getDuration())
                        .videoHash(resultHash)
                        .type(existing.getType())
                        .build();
            } else {
//                log.info("No changes for: "  + rootPath + " " + title);
                return null; // lub pomiń zapis
            }


        }else {
            String codec = FFprobeHelper.getVideoCodec(path.toAbsolutePath().toString());
            String audio = FFprobeHelper.getAudioCodec(path.toAbsolutePath().toString());
            double duration = FFprobeHelper.getVideoDurationInSeconds(path.toAbsolutePath());
            log.info("New video: "  + rootPath + " "  + title);
            log.info("Path: " + path);

            return MediaItemEntity.builder()
                    .title(title)
                    .parentTitle(parentTitle) // było w VideoDetailsEntity
                    .seasonNumber(season)     // było w VideoDetailsEntity
                    .episodeNumber(episode)   // było w VideoDetailsEntity
                    .type(mediaTypeEnum)               // MOVIE / EPISODE
                    .rootPath(rootPath)       // było w VideoInfoEntity
                    .fileName(fileNameWithExtension) // było w VideoDetailsEntity
                    .codec(codec)             // było w VideoTechnicalDetailsEntity
                    .audio(audio)             // było w VideoTechnicalDetailsEntity
                    .duration(duration)
                    .videoHash(resultHash)    // było w VideoTechnicalDetailsEntity
                    .type(mediaTypeEnum)
                    .build();

        }
    }


    public void removeNotExistingMediaItems(Set<String> currentRelativePaths) {
        List<MediaItemEntity> allInDb = mediaItemRepository.findAll();

        List<MediaItemEntity> toDelete = allInDb.stream()
                .filter(video -> {
                    String dbRelativePath = video.getRootPath() + "/" + video.getFileName();
                    return !currentRelativePaths.contains(dbRelativePath);
                })
                .toList();

        mediaItemRepository.deleteAll(toDelete);
    }

}
