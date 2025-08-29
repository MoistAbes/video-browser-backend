package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.api.models.MovieMetadataDto;
import dev.zymion.video.browser.app.api.services.MovieMetadataApiService;
import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import dev.zymion.video.browser.app.models.entities.show.ShowStructureEntity;
import dev.zymion.video.browser.app.repositories.show.GenreRepository;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.repositories.show.ShowRepository;
import dev.zymion.video.browser.app.repositories.show.ShowStructureRepository;
import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import dev.zymion.video.browser.app.services.util.StringUtilService;
import dev.zymion.video.browser.app.services.util.VideoScannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoService {

    private final Path videoFolder;
    private final AppPathProperties appPathProperties;
    private final VideoScannerService videoScannerService;
    private final ShowService showService;
    private final MediaItemRepository mediaItemRepository;
    private final ShowRepository showRepository;
    private final ShowStructureRepository showStructureRepository;
    private final MovieMetadataApiService movieMetadataApiService;
    private final StringUtilService stringUtilService;
    private final GenreRepository genreRepository;

    @Autowired
    public VideoService(AppPathProperties appPathProperties, VideoScannerService videoScannerService, ShowService showService, MediaItemRepository mediaItemRepository, ShowRepository showRepository, ShowStructureRepository showStructureRepository, MovieMetadataApiService movieMetadataApiService, StringUtilService stringUtilService, GenreRepository genreRepository) {
        this.appPathProperties = appPathProperties;
        this.videoScannerService = videoScannerService;
        this.showService = showService;
        this.mediaItemRepository = mediaItemRepository;
        this.videoFolder = appPathProperties.getVideoFolder();
        this.showRepository = showRepository;
        this.showStructureRepository = showStructureRepository;
        this.movieMetadataApiService = movieMetadataApiService;
        this.stringUtilService = stringUtilService;
        this.genreRepository = genreRepository;
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
        List<Path> files = videoScannerService.findAllVideoFiles(videoFolder);

        // 1️⃣ Zbieramy wszystkie aktualne relativePath-y
        Set<String> currentRelativePaths = files.stream()
                .map(path -> videoFolder.relativize(path).toString().replace("\\", "/"))
                .collect(Collectors.toSet());

        // 2️⃣ Przetwarzamy nowe/zmienione pliki
        List<MediaItemEntity> mediaItems = files.stream()
                .map(this::buildEntityFromPath)
                .filter(Objects::nonNull)
                .toList();


        //tutaj beda tylko te ktore sie zmienily lub nowe
        List<MediaItemEntity> savedMediaItems = mediaItemRepository.saveAll(mediaItems);

        // 3️⃣ Usuwamy z bazy te, których już nie ma na dysku
        removeMissingFiles(currentRelativePaths);

        List<ShowEntity> savedShows = showService.setUpShows(savedMediaItems);


        //ustawianie struktur shows
        setUpShowsStructureType();

        //fetch api tmdb data
        fetchMetadataForShows(savedShows);

    }


    private void fetchMetadataForShows(List<ShowEntity> shows) {

        List<GenreEntity> genres = genreRepository.findAll();


        for (ShowEntity show : shows) {

            //wyciagamy czysty tytuł
            String rawTitle = show.getName();
            String cleanTitle = stringUtilService.extractCleanTitle(rawTitle);
            //wyciagamy potencjalny rok produkcji
            Optional<Integer> yearOpt = stringUtilService.extractYearFromTitle(rawTitle);

            boolean isMovie = false;

            //decydujemy czy jest to film czy nie (potrzebne do fetch api tmdb inne endpointy)
            StructureTypeEnum structure = show.getStructure().getName();
            if (structure == StructureTypeEnum.SINGLE_MOVIE || structure == StructureTypeEnum.MOVIE_COLLECTION) {
                isMovie = true;
            }


            Optional<MovieMetadataDto> showMetadata = movieMetadataApiService.fetchMetadata(cleanTitle, yearOpt, isMovie, genres);

            //jesli znajdzie dane
            showMetadata.ifPresent(movieMetadataDto -> show.setGenres(movieMetadataDto.getGenres()));
        }
        showRepository.saveAll(shows);

    }


    private MediaItemEntity buildEntityFromPath(Path path)   {
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
            mediaTypeEnum = MediaTypeEnum.EPISODE;
        }

//        System.out.println(title + " | " + relativePath + " | " + parentTitle + " | " + season + " | " + episode);
        String resultHash = computeMetadataHash(path);

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

    private void removeMissingFiles(Set<String> currentRelativePaths) {
        List<MediaItemEntity> allInDb = mediaItemRepository.findAll();

        List<MediaItemEntity> toDelete = allInDb.stream()
                .filter(video -> {
                    String dbRelativePath = video.getRootPath() + "/" + video.getFileName();
                    return !currentRelativePaths.contains(dbRelativePath);
                })
                .toList();

        mediaItemRepository.deleteAll(toDelete);
    }

    private String computeMetadataHash(Path path) {
        try {
            long size = Files.size(path);
            long modified = Files.getLastModifiedTime(path).toMillis();
            String key = path.toAbsolutePath().toString() + size + modified;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot compute metadata hash", e);
        }
    }

    private void setUpShowsStructureType() {
        List<ShowEntity> allShows = showRepository.findAll();

        for (ShowEntity show : allShows) {
            StructureTypeEnum type = StructureTypeEnum.fromShow(show);

            ShowStructureEntity structure = showStructureRepository.findByName(type)
                    .orElseThrow(() -> new IllegalStateException("Brak zdefiniowanej struktury: " + type));

            show.setStructure(structure);
        }

        showRepository.saveAll(allShows);
    }

    public Resource getImageResource(String relativePath) throws FileNotFoundException, MalformedURLException {
        Path fullPath = videoFolder.resolve(relativePath).normalize();

        // Zabezpieczenie przed wyjściem poza folder
        if (!fullPath.startsWith(videoFolder.toAbsolutePath())) {
            throw new SecurityException("Attempt to access outside of video folder");
        }

        if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
            throw new FileNotFoundException("Icon not found: " + relativePath);
        }

        return new UrlResource(fullPath.toUri());
    }

    public Resource getSubtitles(String relativePath, String subtitleName) throws MalformedURLException, FileNotFoundException {

        Path subtitlePath = appPathProperties.getVideoFolder()             // E:/VIDEO
                .resolve(relativePath)                                     // /MOVIE/Fast and Furious/Fast and Furious 1
                .resolve(appPathProperties.getSubtitleFolder())            // /subtitles
                .resolve(subtitleName + ".vtt")                            // /Fast and Furious 1.vtt
                .normalize();                                              // Normalized full path

        if (!Files.exists(subtitlePath)) {
            throw new FileNotFoundException("Subtitle not found: " + subtitlePath);
        }

        return new UrlResource(subtitlePath.toUri());
    }

}
