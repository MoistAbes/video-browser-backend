package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.services.file.FileService;
import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import dev.zymion.video.browser.app.services.util.VideoFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MediaItemService {

    private final MediaItemRepository mediaItemRepository;
    private final Path videoFolder;
    private final FileService fileService;
    private final VideoFileUtils videoFileUtils;

    public MediaItemService(MediaItemRepository mediaItemRepository, AppPathProperties appPathProperties, FileService fileService, VideoFileUtils videoFileUtils) {
        this.mediaItemRepository = mediaItemRepository;
        this.videoFolder = appPathProperties.getVideoFolder();
        this.fileService = fileService;
        this.videoFileUtils = videoFileUtils;
    }

    public MediaItemEntity buildEntityFromPath(Path path) {
        // 1️⃣ Wyciągamy podstawowe informacje o pliku
        String fileName = path.getFileName().toString();
        String title = fileName.replaceFirst("\\.[^.]+$", "");

        FilePathInfo pathInfo = extractFilePathInfo(path);

        // 2️⃣ Parsujemy metadane: typ, sezon, odcinek, parentTitle
        MediaMetadata metadata = parseMediaMetadata(pathInfo.parts(), title);

        // 3️⃣ Obliczamy hash pliku
        String resultHash = fileService.computeMetadataHash(path);

        // 4️⃣ Sprawdzamy, czy plik już istnieje w bazie
        MediaItemEntity existing = mediaItemRepository.findByVideoHash(resultHash).orElse(null);

        // 5️⃣ Tworzymy lub aktualizujemy encję
        return buildOrUpdateEntity(existing, title, pathInfo.rootPath(), pathInfo.fileNameWithExtension(), metadata, path, resultHash);
    }

    ///////////////////// Prywatne metody pomocnicze /////////////////////

    private MediaItemEntity buildOrUpdateEntity(MediaItemEntity existing,
                                                String title,
                                                String rootPath,
                                                String fileNameWithExtension,
                                                MediaMetadata metadata,
                                                Path path,
                                                String resultHash) {

        if (existing != null) {
            if (!existing.getVideoHash().equals(resultHash)) {
                log.info("File modified: " + rootPath + " " + title);
                return MediaItemEntity.builder()
                        .id(existing.getId())
                        .title(title)
                        .rootPath(rootPath)
                        .fileName(fileNameWithExtension)
                        .parentTitle(metadata.parentTitle())
                        .seasonNumber(metadata.season())
                        .episodeNumber(metadata.episode())
                        .codec(existing.getCodec().orElse(null))
                        .audio(existing.getAudio().orElse(null))
                        .duration(existing.getDuration())
                        .videoHash(resultHash)
                        .type(existing.getType())
                        .build();
            } else {
                return null; // brak zmian
            }
        } else {
            String codec = FFprobeHelper.getVideoCodec(path.toAbsolutePath().toString());
            String audio = FFprobeHelper.getAudioCodec(path.toAbsolutePath().toString());
            double duration = FFprobeHelper.getVideoDurationInSeconds(path.toAbsolutePath());

            log.info("New video: " + rootPath + " " + title);
            log.info("Path: " + path);

            return MediaItemEntity.builder()
                    .title(title)
                    .parentTitle(metadata.parentTitle())
                    .seasonNumber(metadata.season())
                    .episodeNumber(metadata.episode())
                    .type(metadata.type())
                    .rootPath(rootPath)
                    .fileName(fileNameWithExtension)
                    .codec(codec)
                    .audio(audio)
                    .duration(duration)
                    .videoHash(resultHash)
                    .build();
        }
    }

    private record FilePathInfo(String relativePath, String rootPath, String[] parts, String fileNameWithExtension) {}

    private FilePathInfo extractFilePathInfo(Path path) {
        // relativePath względem videoFolder, z normalizacją separatorów
        String relativePath = videoFileUtils.toRelativePath(path, videoFolder); // np. "Demon Slayer/Movie/Infinity Castle/Demon Slayer - Infinity Castle.mp4"

        // Użyjemy obiektu Path do bezpiecznego wyciągnięcia rodzica (katalogu)
        Path relative = Path.of(relativePath); // Path użyje "/" w tej formie na większości JDK
        Path parent = relative.getParent();

        // rootPath ma być katalogiem, w którym znajduje się plik (albo sam relativePath jeśli relative to katalog)
        String rootPath = (parent != null) ? parent.toString().replace("\\", "/") : relative.toString().replace("\\", "/");

        // parts i fileNameWithExtension bazujemy na relativePath (znormalizowanym)
        String[] parts = relativePath.split("/"); // już normalizowane w videoFileUtils
        String fileNameWithExtension = parts[parts.length - 1];

        return new FilePathInfo(relativePath, rootPath, parts, fileNameWithExtension);
    }



    private record MediaMetadata(MediaTypeEnum type, String parentTitle, Integer season, Integer episode) {}

    private MediaMetadata parseMediaMetadata(String[] parts, String title) {
        MediaTypeEnum type = MediaTypeEnum.MOVIE;
        String parentTitle = parts[0];
        Integer season = null;
        Integer episode = null;

        // Szukamy wzorca SxxEyy w tytule
        Matcher epMatcher = Pattern.compile("S(\\d{1,2})E(\\d{1,2})", Pattern.CASE_INSENSITIVE).matcher(title);

        if (epMatcher.find()) {
            season = Integer.parseInt(epMatcher.group(1));
            episode = Integer.parseInt(epMatcher.group(2));
            type = MediaTypeEnum.TV;
        }

        return new MediaMetadata(type, parentTitle, season, episode);
    }




    public void removeNotExistingMediaItems(Set<String> currentRelativePaths) {
        List<MediaItemEntity> toDelete = mediaItemRepository.findNotExisting(currentRelativePaths);

        if (!toDelete.isEmpty()) {
            log.info("Usuwanie {} rekordów, które nie istnieją na dysku:", toDelete.size());

            toDelete.forEach(item ->
                    log.info("Parent title = {} - Title = {}, rootPath = {}", item.getParentTitle(), item.getTitle(), item.getRootPath())
            );

            mediaItemRepository.deleteAll(toDelete);
        }
    }



    public void convertMediaItemsAudioCodec() {

        List<MediaItemEntity> mediaItemsToConvert =
                mediaItemRepository.findAllWithUnsupportedAudioCodecs(Arrays.asList("ac3", "eac3"));

         fileService.transcodeMediaItemsSequentially(mediaItemsToConvert);



    }
}
