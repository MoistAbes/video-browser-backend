package dev.zymion.video.browser.app.services.file;

import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {

    private final AppPathProperties appPathProperties;
    private final Path videoFolder;

    public FileService(AppPathProperties appPathProperties) {
        this.appPathProperties = appPathProperties;
        this.videoFolder = appPathProperties.getVideoFolder();

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
            throw new FileNotFoundException("Subtitle not found: " + subtitlePath);
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
                    .filter(this::isVideoFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Błąd podczas skanowania plików wideo w folderze: " + root, e);
        }
    }


    private boolean isVideoFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi");
    }
}
