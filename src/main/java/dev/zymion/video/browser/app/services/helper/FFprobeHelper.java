package dev.zymion.video.browser.app.services.helper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class FFprobeHelper {

    public static double getVideoDurationInSeconds(Path filePath) {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",            // minimalne logi
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                filePath.toString()
        );

        try {
            Process process = pb.start();
            String durationStr;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                durationStr = br.readLine(); // odczytujemy tylko pierwszą linię
            }

            process.waitFor();

            if (durationStr != null && !durationStr.isEmpty()) {
                return Double.parseDouble(durationStr); // czas w sekundach
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            System.err.println("Błąd odczytu czasu trwania pliku " + filePath + ": " + e.getMessage());
        }

        return 0.0;
    }



    public static String getVideoCodec(String videoPath) {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=codec_name",
                "-of", "csv=p=0",
                videoPath
        );

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String codec = reader.readLine();
            process.waitFor();
            return codec;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAudioCodec(String videoPath) {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "a:0",
                "-show_entries", "stream=codec_name",
                "-of", "csv=p=0",
                videoPath
        );

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String codec = reader.readLine();
            process.waitFor();

            if (codec != null) {
                // Usuwa spacje, przecinki, enter z końca (bezpieczne oczyszczenie)
                return codec.trim().replaceAll(",$", "");
            }

            return null;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
