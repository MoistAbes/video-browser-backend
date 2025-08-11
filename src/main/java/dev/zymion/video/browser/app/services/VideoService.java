package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.entities.VideoDetailsEntity;
import dev.zymion.video.browser.app.entities.VideoInfoEntity;
import dev.zymion.video.browser.app.entities.VideoTechnicalDetailsEntity;
import dev.zymion.video.browser.app.enums.VideoTypeEnum;
import dev.zymion.video.browser.app.models.VideoInfo;
import dev.zymion.video.browser.app.repositories.ShowRepository;
import dev.zymion.video.browser.app.repositories.VideoInfoRepository;
import dev.zymion.video.browser.app.services.helper.AppPathProperties;
import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VideoService {

    private final VideoInfoRepository videoInfoRepository;
    private final Path videoFolder = Paths.get("E:/VIDEO");
    private final AppPathProperties appPathProperties;
    private final VideoScannerService videoScannerService;
    private final ShowService showService;
    private final ShowRepository showRepository;

    @Autowired
    public VideoService(VideoInfoRepository videoInfoRepository, AppPathProperties appPathProperties, VideoScannerService videoScannerService, ShowService showService, ShowRepository showRepository) {
        this.appPathProperties = appPathProperties;
        this.videoScannerService = videoScannerService;
        this.videoInfoRepository = videoInfoRepository;
        this.showService = showService;
        this.showRepository = showRepository;
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
        //kasujemy poprzednie
        showRepository.deleteAll();

        List<Path> files = videoScannerService.findAllVideoFiles(videoFolder);

        // 1️⃣ Zbieramy wszystkie aktualne relativePath-y
        Set<String> currentRelativePaths = files.stream()
                .map(path -> videoFolder.relativize(path).toString().replace("\\", "/"))
                .collect(Collectors.toSet());

        // 2️⃣ Przetwarzamy nowe/zmienione pliki
        List<VideoInfoEntity> entities = files.stream()
                .map(this::buildEntityFromPath)
                .filter(Objects::nonNull) // pomija pliki bez zmian
                .collect(Collectors.toList());

        videoInfoRepository.saveAll(entities);

        // 3️⃣ Usuwamy z bazy te, których już nie ma na dysku
        removeMissingFiles(currentRelativePaths);

        showService.setUpShows(videoInfoRepository.findAll());


    }


    private VideoInfoEntity buildEntityFromPath(Path path) {
        String fileName = path.getFileName().toString();
        String title = fileName.replaceFirst("\\.[^.]+$", "");

        // Ścieżka względem folderu głównego np. ANIME/ReZero/Season 2/02.mp4
        String relativePath = videoFolder.relativize(path).toString().replace("\\", "/");
        String rootPath = relativePath.substring(0, relativePath.lastIndexOf("/"));
        String[] parts = relativePath.split("/");

        String fileNameWithExtension = parts[parts.length - 1];

        // Typ = pierwsza część ścieżki (np. ANIME/MOVIE)
        String typeRaw = parts[0];
        VideoTypeEnum type = VideoTypeEnum.fromString(typeRaw);

        // Ikona
        String iconName = findIconPath(path);

        String parentTitle = null;
        Integer season = null;
        Integer episode = null;


        if (type.equals(VideoTypeEnum.MOVIE)) {
            parentTitle = parts[1];
        } else if (parts.length >= 3) {
            // np. SHOWS/BreakingBad/Season1/S01E01.mp4
            parentTitle = parts[1];

            // Parsujemy sezon z folderu, np. Season1 → 1
            Matcher seasonMatcher = Pattern.compile("Season\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(parts[2]);
            if (seasonMatcher.find()) {
                season = Integer.parseInt(seasonMatcher.group(1));
            }


            // Parsujemy odcinek z nazwy pliku, np. S01E03
            Matcher epMatcher = Pattern.compile("S\\d{1,2}E(\\d{1,2})", Pattern.CASE_INSENSITIVE).matcher(title);
            if (epMatcher.find()) {
                episode = Integer.parseInt(epMatcher.group(1));
            }
        }

//        System.out.println(title + " | " + relativePath + " | " + parentTitle + " | " + season + " | " + episode);
        String resultHash = computeMetadataHash(path);
        Optional<VideoInfoEntity> optionalVideoInfo =
                videoInfoRepository.findByRootPathAndVideoDetails_FileName(rootPath, fileNameWithExtension);
//        System.out.println(path + " | resultHash: " + resultHash);

        // String codec = FFprobeHelper.getVideoCodec(path.toAbsolutePath().toString());
        // String audio = FFprobeHelper.getAudioCodec(path.toAbsolutePath().toString());

        if (optionalVideoInfo.isPresent()) {
            VideoInfoEntity existing = optionalVideoInfo.get();

            if (!existing.getVideoTechnicalDetails().getVideoHash().equals(resultHash)) {
                log.info("File modified: " + rootPath + " " + title);
                // zwróć zaktualizowany obiekt
                return VideoInfoEntity.builder()
                        .id(existing.getId())
                        .title(title)
                        .type(type)
                        .rootPath(rootPath)
                        .iconFileName(iconName)
                        .videoDetails(VideoDetailsEntity.builder()
                                .id(existing.getVideoDetails().getId())
                                .fileName(fileNameWithExtension)
                                .parentTitle(parentTitle)
                                .season(season)
                                .episode(episode)
                                .build())
                        .videoTechnicalDetails(VideoTechnicalDetailsEntity.builder()
                                .id(existing.getVideoTechnicalDetails().getId())
                                .videoHash(resultHash)
//                        .codec(codec)
//                        .audio(audio)
                                .build())
                        .build();
            } else {
                log.info("No changes for: "  + rootPath + " " + title);
                return null; // lub pomiń zapis
            }


        }else {
            log.info("New video info: "  + rootPath + " "  + title);

            return VideoInfoEntity.builder()
                    .title(title)
                    .type(type)
                    .rootPath(rootPath)
                    .iconFileName(iconName)
                    .videoDetails(VideoDetailsEntity.builder()
                            .fileName(fileNameWithExtension)
                            .parentTitle(parentTitle)
                            .season(season)
                            .episode(episode)
                            .build())
                    .videoTechnicalDetails(VideoTechnicalDetailsEntity.builder()
                            .videoHash(resultHash)
//                        .codec(codec)
//                        .audio(audio)
                            .build())
                    .build();
        }
    }


    private void removeMissingFiles(Set<String> currentRelativePaths) {
        List<VideoInfoEntity> allInDb = videoInfoRepository.findAll();

        List<VideoInfoEntity> toDelete = allInDb.stream()
                .filter(video -> {
                    String dbRelativePath = video.getRootPath() + "/" + video.getVideoDetails().getFileName();
                    return !currentRelativePaths.contains(dbRelativePath);
                })
                .toList();

        videoInfoRepository.deleteAll(toDelete);
        log.info("Removed " + toDelete.size() + " entries not found on disk");
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


    private String findIconPath(Path path) {
        Path iconDir = path.getParent().resolve("icon");
        if (Files.exists(iconDir) && Files.isDirectory(iconDir)) {
            try (Stream<Path> iconFiles = Files.list(iconDir)) {
                return iconFiles
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString()) // tylko nazwa pliku z rozszerzeniem
                        .findFirst()
                        .orElse("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }


    public List<String> getAllThumbnails(String rootFolderPath) {
        List<String> thumbnails = new ArrayList<>();

        // Załóżmy, że masz pole baseVideoFolder ustawione gdzieś wcześniej
        Path thumbnailsDir = Paths.get(String.valueOf(videoFolder), rootFolderPath, "thumbnails");

        if (Files.exists(thumbnailsDir) && Files.isDirectory(thumbnailsDir)) {
            try (Stream<Path> paths = Files.list(thumbnailsDir)) {
                thumbnails = paths
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString()) // tylko nazwy plików
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return thumbnails;
    }


    public void getVideoStream(String relativePath, HttpServletRequest request, HttpServletResponse response) throws IOException {


        Path filePath = videoFolder.resolve(relativePath).normalize();

        if (!Files.exists(filePath) || !filePath.startsWith(videoFolder)) {
            log.info("Plik nie istnieje lub ścieżka jest nieprawidłowa: " + filePath);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = Files.size(filePath);
        String contentType = Files.probeContentType(filePath);
        String rangeHeader = request.getHeader("Range");

        long start = 0;
        long end = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }
        }

        if (end >= fileLength) {
            end = fileLength - 1;
        }

        long contentLength = end - start + 1;

        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);


        try (SeekableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.READ);
             ServletOutputStream outputStream = response.getOutputStream()) {

            channel.position(start);

            int bufferSize = 1024 * 1024; // 1MB dla lepszej wydajności przy 4K
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            long bytesToRead = contentLength;

            while (bytesToRead > 0) {
                buffer.clear();
                int bytesRead = channel.read(buffer);
                if (bytesRead == -1) break;

                buffer.flip();
                int toWrite = (int) Math.min(bytesRead, bytesToRead);

                outputStream.write(buffer.array(), 0, toWrite);
                bytesToRead -= toWrite;
            }
        }


//        try (InputStream inputStream = Files.newInputStream(filePath);
//             ServletOutputStream outputStream = response.getOutputStream()) {
//
//            long skippedTotal = 0;
//            while (skippedTotal < start) {
//                long skipped = inputStream.skip(start - skippedTotal);
//                if (skipped <= 0) {
//                    throw new IOException("Nie udało się pominąć bajtów w InputStream");
//                }
//                skippedTotal += skipped;
//            }
//
//            byte[] buffer = new byte[8192];
//            long bytesToRead = contentLength;
//            int bytesRead;
//
//            while (bytesToRead > 0 && (bytesRead = inputStream.read(buffer, 0, (int)Math.min(buffer.length, bytesToRead))) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//                bytesToRead -= bytesRead;
//            }
//        }

    }


    public Resource getVideoIcon(String relativePath) throws FileNotFoundException, MalformedURLException {
        Path fullPath = videoFolder.resolve(relativePath).normalize();

        System.out.println("full path = " + fullPath);

        // Zabezpieczenie przed wyjściem poza folder
        if (!fullPath.startsWith(videoFolder.toAbsolutePath())) {
            throw new SecurityException("Attempt to access outside of video folder");
        }

        if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
            throw new FileNotFoundException("Icon not found: " + relativePath);
        }

        return new UrlResource(fullPath.toUri());
    }


    public Resource getSeasonBackdrop(String relativePath) throws FileNotFoundException, MalformedURLException {
        Path fullPath = videoFolder.resolve(relativePath).normalize();

        System.out.println("full path = " + fullPath);

        // Zabezpieczenie przed wyjściem poza folder
        if (!fullPath.startsWith(videoFolder.toAbsolutePath())) {
            throw new SecurityException("Attempt to access outside of video folder");
        }

        if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
            throw new FileNotFoundException("Backdrop not found: " + relativePath);
        }

        return new UrlResource(fullPath.toUri());

    }

    public Resource getSubtitles(String relativePath, String subtitleName) throws MalformedURLException, FileNotFoundException {

        Path subtitlePath = appPathProperties.getVideoFolder()             // E:/VIDEO
                .resolve(relativePath)                                     // /MOVIE/Fast and Furious/Fast and Furious 1
                .resolve(appPathProperties.getSubtitleFolder())            // /subtitles
                .resolve(subtitleName + ".vtt")                            // /Fast and Furious 1.vtt
                .normalize();                                              // Normalized full path

        System.out.println("subtitlePath = " + subtitlePath);

        if (!Files.exists(subtitlePath)) {
            throw new FileNotFoundException("Subtitle not found: " + subtitlePath);
        }

        return new UrlResource(subtitlePath.toUri());
    }



}
