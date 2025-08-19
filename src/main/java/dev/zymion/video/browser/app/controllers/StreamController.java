package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.services.StreamService;
import dev.zymion.video.browser.app.services.helper.AppPathProperties;
import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@CrossOrigin(origins = "http://localhost:4200")  // dopasuj do swojego frontu
@RestController
@RequestMapping("/stream")
@Slf4j
public class StreamController {
    private final AppPathProperties appPathProperties;
    private final StreamService streamService;

    public StreamController(AppPathProperties appPathProperties, StreamService streamService) {
        this.appPathProperties = appPathProperties;
        this.streamService = streamService;
    }

    @GetMapping(value = "/convert", produces = "video/mp4")
    public void streamConvertVideo(
            @RequestParam("path") String relativePath,
            @RequestParam(value = "start", defaultValue = "0") double startTime,
            HttpServletResponse response) throws IOException {

        log.info("Konwertuję wideo: {} od sekundy: {}", relativePath, startTime);

        Path filePath = appPathProperties.getVideoFolder()
                .resolve(relativePath)
                .normalize();

        // Odpalenie konwersji z ffmpeg od konkretnego czasu
        FFprobeHelper.streamWithAudioConversion(filePath, startTime, response);
    }



    @GetMapping(value = "/normal", produces = "video/mp4")
    public void streamVideo(@RequestParam("path") String relativePath,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0); // brak limitu czasu

        asyncContext.start(() -> {
            try {
                streamService.getStream(relativePath, request, response);
            } catch (IOException e) {
                // ignorujemy przerwane połączenie (przewijanie, zamknięcie okna)
                if (e.getMessage() == null || !e.getMessage().contains("Connection reset")) {
                    log.error("Błąd podczas streamowania wideo", e);
                }
            } catch (Exception e) {
                log.error("Nieoczekiwany błąd podczas streamowania", e);
            } finally {
                try {
                    asyncContext.complete();
                } catch (IllegalStateException ignored) {
                    // już zakończone
                }
            }
        });
    }


}
