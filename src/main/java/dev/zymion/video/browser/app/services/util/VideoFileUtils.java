package dev.zymion.video.browser.app.services.util;

import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VideoFileUtils {

    // zestaw wspieranych rozszerzeń wideo
    private static final Set<String> SUPPORTED_VIDEO_EXTENSIONS = Set.of(
            "mp4", "mkv", "avi"
    );

    /**
     * Sprawdza, czy plik jest wideo na podstawie rozszerzenia.
     * @param path ścieżka do pliku
     * @return true, jeśli plik jest obsługiwanym wideo
     */
    public boolean isVideoFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return false;  // brak rozszerzenia
        String extension = filename.substring(dotIndex + 1);
        return SUPPORTED_VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * Jeśli chcesz, możesz też udostępnić metodę zwracającą listę wszystkich wspieranych rozszerzeń
     */
    public Set<String> getSupportedVideoExtensions() {
        return Collections.unmodifiableSet(SUPPORTED_VIDEO_EXTENSIONS);
    }

    public String toRelativePath(Path file, Path baseFolder) {
        if (file == null || baseFolder == null) return null;

        Path normalizedBase = baseFolder.toAbsolutePath().normalize();
        Path normalizedFile = file.toAbsolutePath().normalize();

        return normalizedBase
                .relativize(normalizedFile)
                .toString()
                .replace("\\", "/");
    }


    /**
     * Tworzy zestaw względnych ścieżek plików względem folderu bazowego
     * i normalizuje je do formatu z "/".
     *
     * @param files lista plików do przetworzenia
     * @param baseFolder folder bazowy
     * @return zbiór relativePath-ów w formacie String
     */
    public Set<String> toRelativePaths(Set<Path> files, Path baseFolder) {
        return files.stream()
                .map(path -> toRelativePath(path, baseFolder)) // korzystamy z pojedynczej metody
                .collect(Collectors.toSet());
    }


}
