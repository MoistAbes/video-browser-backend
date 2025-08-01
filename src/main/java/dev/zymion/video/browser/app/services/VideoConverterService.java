package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VideoConverterService {

    //ToDO poki co nie uzywane bo konwersja zdaje sie nie dzialac
//    public Path convertIfNeeded(Path original) {
////        String codec = FFprobeHelper.getVideoCodec(original.toString());
////        String audio = FFprobeHelper.getAudioCodec(original.toString());
//
//        if (true) {
//            return original;
//        }
//
////        if ("h264".equals(codec) && ("aac".equals(audio) || "mp3".equals(audio))) {
////            return original;
////        }
//
//        String fileName = original.getFileName().toString().replaceFirst("\\.[^.]+$", "") + "_converted.mp4";
//        Path converted = original.getParent().resolve(fileName);
//
//        try {
//            ProcessBuilder ffmpeg = new ProcessBuilder(
//                    "ffmpeg", "-i", original.toString(),
//                    "-c:v", "libx264", "-c:a", "aac", "-y",
//                    converted.toString()
//            );
//            ffmpeg.inheritIO();
//            Process p = ffmpeg.start();
//            p.waitFor();
//            return Files.exists(converted) ? converted : original;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return original;
//        }
//    }


}
