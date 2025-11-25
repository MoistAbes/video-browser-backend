package dev.zymion.video.browser.app.services.file;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import dev.zymion.video.browser.app.repositories.show.MediaItemRepository;
import dev.zymion.video.browser.app.services.util.VideoFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileService {

    private final AppPathProperties appPathProperties;
    private final Path videoFolder;
    private final MediaItemRepository mediaItemRepository;
    private final VideoFileUtils videoFileUtils;


    public FileService(AppPathProperties appPathProperties, MediaItemRepository mediaItemRepository, VideoFileUtils videoFileUtils) {
        this.appPathProperties = appPathProperties;
        this.videoFolder = appPathProperties.getVideoFolder();
        this.mediaItemRepository = mediaItemRepository;
        this.videoFileUtils = videoFileUtils;
    }


    public String computeMetadataHash(Path path) {
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
            log.warn("Subtitle not found: {}", subtitlePath);
            return null; // albo Optional.empty()
        }

        return new UrlResource(subtitlePath.toUri());
    }

    public List<Path> findAllVideoFiles(Path root) {
        if (!Files.exists(root)) {
            throw new IllegalArgumentException("Ścieżka nie istnieje: " + root);
        }

        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Ścieżka nie jest folderem: " + root);
        }

        if (!Files.isReadable(root)) {
            throw new IllegalStateException("Brak uprawnień do odczytu folderu: " + root);
        }

        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(videoFileUtils::isVideoFile)  // <-- używamy nowego utility
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Błąd podczas skanowania plików wideo w folderze: " + root, e);
        }

    }


    public void transcodeMediaItemsSequentially(List<MediaItemEntity> mediaItems) {
        List<MediaItemEntity> successfullyTranscoded = new ArrayList<>();

        for (MediaItemEntity item : mediaItems) {
            try {
                if (transcodeSingleMediaItem(item)) {
                    item.setAudio("aac");
                    successfullyTranscoded.add(item);
                }
            } catch (Exception e) {
                log.error("Błąd transkodowania {}: {}", item.getFileName(), e.getMessage());
            }
        }

        // aktualizacja
        if (!successfullyTranscoded.isEmpty()) {
            mediaItemRepository.saveAll(successfullyTranscoded);
            log.info("Zaktualizowano {} encji (audio = aac)", successfullyTranscoded.size());
        }

        // 🔥 KLUCZOWE — chwila na zwolnienie uchwytów
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // dopiero teraz zamieniamy
        finalizeTranscodes(successfullyTranscoded);
    }

    private boolean transcodeSingleMediaItem(MediaItemEntity item) throws Exception {
        Path originalFile = videoFolder
                .resolve(item.getRootPath())
                .resolve(item.getFileName());

        String fileName = originalFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String ext = dot == -1 ? "" : fileName.substring(dot + 1);

        Path transcodedFile = originalFile.resolveSibling(
                fileName.replace("." + ext, ".transcoded." + ext)
        );

        log.info("Transkoduję: {}", originalFile);
        log.info("Plik wyjściowy: {}", transcodedFile);

        List<String> cmd = List.of(
                "ffmpeg", "-y",
                "-i", originalFile.toString(),
                "-map", "0:v:0",
                "-map", "0:a:0",
                "-c:v", "copy",
                "-c:a", "aac", "-b:a", "192k",
                "-movflags", "faststart",
                transcodedFile.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // ---- ODCZYT OUTPUTU → STRUMIENI NIE MOŻE ZOSTAĆ OTWARTY! ----
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                log.info("[FFMPEG] {}", line);
            }
        }

        // ---- najważniejsze miejsce → czekamy aż OS zwolni uchwyty ----
        int exit = process.waitFor();

        // pewność, że proces nie ma uchwytów
        process.getInputStream().close();
        process.getErrorStream().close();
        process.getOutputStream().close();
        process.destroy();

        log.info("FFmpeg exit: {}", exit);

        if (exit != 0) {
            log.error("Błąd transkodowania {}", originalFile);
            Files.deleteIfExists(transcodedFile);
            return false;
        }

        return true;
    }

    private void finalizeTranscodes(List<MediaItemEntity> items) {
        for (MediaItemEntity item : items) {
            try {
                Path original = videoFolder
                        .resolve(item.getRootPath())
                        .resolve(item.getFileName());

                String fileName = original.getFileName().toString();
                int dot = fileName.lastIndexOf('.');
                String ext = dot == -1 ? "" : fileName.substring(dot + 1);

                Path transcoded = original.resolveSibling(
                        fileName.replace("." + ext, ".transcoded." + ext)
                );

                if (!Files.exists(transcoded)) {
                    log.warn("Brak pliku transkodowanego: {}", transcoded);
                    continue;
                }

                log.info("Zamieniam {} → {}", transcoded, original);

                boolean moved = false;
                int attempts = 0;

                while (!moved && attempts < 5) {
                    try {
                        if (Files.exists(original)) {
                            Files.delete(original); // jeśli blokowany → exception
                        }

                        Files.move(transcoded, original, StandardCopyOption.REPLACE_EXISTING);
                        moved = true;

                    } catch (Exception e) {
                        attempts++;
                        log.warn("Próba {} nieudana dla {} (powód: {})",
                                attempts, original, e.getMessage());
                        Thread.sleep(200); // krótka przerwa na zwolnienie uchwytu
                    }
                }

                if (!moved) {
                    log.error("❌ Nie udało się zamienić pliku {}", original);
                }

            } catch (Exception e) {
                log.error("Błąd przy finalize: {}", e.getMessage());
            }
        }
    }





}
