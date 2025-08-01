package dev.zymion.video.browser.app.services.helper;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FFprobeHelper {

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
