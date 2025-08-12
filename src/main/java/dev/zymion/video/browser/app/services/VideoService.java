package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.entities.MediaItemEntity;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.repositories.MediaItemRepository;
import dev.zymion.video.browser.app.repositories.ShowRepository;
import dev.zymion.video.browser.app.services.helper.AppPathProperties;
import dev.zymion.video.browser.app.services.util.VideoScannerService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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

    private final Path videoFolder = Paths.get("E:/VIDEO");
    private final AppPathProperties appPathProperties;
    private final VideoScannerService videoScannerService;
    private final ShowService showService;
    private final ShowRepository showRepository;
    private final MediaItemRepository mediaItemRepository;

    @Autowired
    public VideoService(AppPathProperties appPathProperties, VideoScannerService videoScannerService, ShowService showService, ShowRepository showRepository, MediaItemRepository mediaItemRepository) {
        this.appPathProperties = appPathProperties;
        this.videoScannerService = videoScannerService;
        this.showService = showService;
        this.showRepository = showRepository;
        this.mediaItemRepository = mediaItemRepository;
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
//        List<VideoInfoEntity> entities = files.stream()
//                .map(this::buildEntityFromPath)
//                .filter(Objects::nonNull) // pomija pliki bez zmian
//                .collect(Collectors.toList());
//
//        videoInfoRepository.saveAll(entities);

        List<MediaItemEntity> mediaItems = files.stream()
                .map(this::buildEntityFromPath)
                .filter(Objects::nonNull)
                .toList();

        mediaItemRepository.saveAll(mediaItems);

        // 3️⃣ Usuwamy z bazy te, których już nie ma na dysku
        removeMissingFiles(currentRelativePaths);

//        showService.setUpShows(videoInfoRepository.findAll());
        showService.setUpShows2(mediaItemRepository.findAll());


    }


    private MediaItemEntity buildEntityFromPath(Path path) {
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

        // String codec = FFprobeHelper.getVideoCodec(path.toAbsolutePath().toString());
        // String audio = FFprobeHelper.getAudioCodec(path.toAbsolutePath().toString());

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
                        .videoHash(resultHash)
                        .build();
            } else {
                log.info("No changes for: "  + rootPath + " " + title);
                return null; // lub pomiń zapis
            }


        }else {
            log.info("New video info: "  + rootPath + " "  + title);

            return MediaItemEntity.builder()
                    .title(title)
                    .parentTitle(parentTitle) // było w VideoDetailsEntity
                    .seasonNumber(season)     // było w VideoDetailsEntity
                    .episodeNumber(episode)   // było w VideoDetailsEntity
                    .type(mediaTypeEnum)               // MOVIE / EPISODE
                    .rootPath(rootPath)       // było w VideoInfoEntity
                    .fileName(fileNameWithExtension) // było w VideoDetailsEntity
//                    .codec(codec)             // było w VideoTechnicalDetailsEntity
//                    .audio(audio)             // było w VideoTechnicalDetailsEntity
                    .videoHash(resultHash)    // było w VideoTechnicalDetailsEntity
//                    .show(showEntity)
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
        log.info("Removed " + toDelete.size() + " entries not found on disk");
    }


//    private void removeMissingFiles(Set<String> currentRelativePaths) {
//        List<VideoInfoEntity> allInDb = videoInfoRepository.findAll();
//
//        List<VideoInfoEntity> toDelete = allInDb.stream()
//                .filter(video -> {
//                    String dbRelativePath = video.getRootPath() + "/" + video.getVideoDetails().getFileName();
//                    return !currentRelativePaths.contains(dbRelativePath);
//                })
//                .toList();
//
//        videoInfoRepository.deleteAll(toDelete);
//        log.info("Removed " + toDelete.size() + " entries not found on disk");
//    }


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
                        .orElse(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
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

        if (!filePath.startsWith(videoFolder) || !Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = Files.size(filePath);
        String contentType = Optional.ofNullable(Files.probeContentType(filePath)).orElse("video/mp4");
        String range = request.getHeader("Range");

        // ETag / Last-Modified
        long lastModified = Files.getLastModifiedTime(filePath, LinkOption.NOFOLLOW_LINKS).toMillis();
        String etag = "\"" + fileLength + "-" + lastModified + "\"";
        response.setHeader("ETag", etag);
        response.setDateHeader("Last-Modified", lastModified);

        // Conditional GET
        if (etag.equals(request.getHeader("If-None-Match")) ||
                (request.getDateHeader("If-Modified-Since") >= 0 && lastModified / 1000 <= request.getDateHeader("If-Modified-Since") / 1000)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        long start = 0, end = fileLength - 1;
        boolean isPartial = false;

        if (range != null && range.startsWith("bytes=")) {
            isPartial = true;
            String spec = range.substring(6).trim(); // e.g. "0-","100-200","-500"
            if (spec.contains(",")) {
                // Simplest path: reject multi-range
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }
            String[] parts = spec.split("-", 2);
            try {
                if (parts[0].isEmpty()) {
                    // suffix range: last N bytes
                    long suffix = Long.parseLong(parts[1]);
                    if (suffix <= 0) throw new NumberFormatException();
                    start = Math.max(fileLength - suffix, 0);
                } else {
                    start = Long.parseLong(parts[0]);
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        end = Long.parseLong(parts[1]);
                    }
                }
            } catch (NumberFormatException ex) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }

            if (start > end || start >= fileLength) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }
            end = Math.min(end, fileLength - 1);
        }

        long contentLength = end - start + 1;

        // If-Range: if validator mismatches, ignore Range and send full content
        String ifRange = request.getHeader("If-Range");
        if (isPartial && ifRange != null && !ifRange.equals(etag)) {
            isPartial = false;
            start = 0; end = fileLength - 1;
            contentLength = fileLength;
        }

        response.setContentType(contentType);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filePath.getFileName().toString() + "\"");

        if (isPartial) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            response.setHeader("Content-Length", String.valueOf(contentLength));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Content-Length", String.valueOf(fileLength));
        }

        // HEAD short-circuit
        if ("HEAD".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        try (FileChannel in = FileChannel.open(filePath, StandardOpenOption.READ);
             ServletOutputStream out = response.getOutputStream()) {

            in.position(start);
            long remaining = contentLength;
            // Efficient copy loop (works well across containers)
            byte[] buf = new byte[1024 * 1024];
            while (remaining > 0) {
                int toRead = (int)Math.min(buf.length, remaining);
                int read = in.read(ByteBuffer.wrap(buf, 0, toRead));
                if (read == -1) break;
                out.write(buf, 0, read);
                remaining -= read;
            }
            out.flush();
        }
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
