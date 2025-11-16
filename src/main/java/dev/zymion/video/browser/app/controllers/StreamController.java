package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.services.StreamKeyService;
import dev.zymion.video.browser.app.services.StreamService;
import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.services.security.SecurityUtilService;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.SocketTimeoutException;


/**
 * Kontroler obsługujący operacje związane ze streamowaniem wideo.
 * Oprócz endpointów do streamowania, zawiera również endpoint autoryzacyjny,
 * który generuje tymczasowy klucz (token) pozwalający na dostęp do strumienia.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/stream")
@Slf4j
public class StreamController {
    private final StreamService streamService;
    private final StreamKeyService streamKeyService;
    private final SecurityUtilService securityUtilService;

    public StreamController(StreamService streamService, StreamKeyService streamKeyService, SecurityUtilService securityUtilService) {
        this.streamService = streamService;
        this.streamKeyService = streamKeyService;
        this.securityUtilService = securityUtilService;
    }


    @GetMapping(value = "/normal")
    public void streamVideo(@RequestParam("path") String relativePath,
                            @RequestParam("authKey") String authKey,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0); // brak limitu czasu

        asyncContext.start(() -> {
            try {
                streamService.getStream(relativePath, request, response, null);
            } catch (ClientAbortException | SocketTimeoutException e) {
                log.debug("Klient przerwał połączenie: {}", e.getClass().getSimpleName());
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
                    log.debug("Klient zresetował połączenie (Connection reset by peer)");
                } else {
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

    @GetMapping("/normal/preview")
    public void streamPreview(@RequestParam("path") String relativePath,
                              @RequestParam("authKey") String authKey,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {

        Long bytes = 150L * 1024 * 1024;

        streamService.getStream(relativePath, request, response, null);
    }

    /**
     * Endpoint, który generuje tymczasowy klucz autoryzacyjny do streamingu.
     *
     * Klucz ten będzie zapisany w Redisie i ważny przez określony czas (np. 1 godzinę).
     * Frontend może go pobrać i przekazywać przy każdym żądaniu streamu,
     * np. w query param: /stream/normal/preview?path=...&authKey=...
     *
     * W przyszłości możesz dodać weryfikację użytkownika (np. JWT) przed wydaniem klucza.
     */
    @GetMapping("/authorize")
    public ResponseEntity<StreamAuthorizeResponse> authorizeStream() {
        String key = streamKeyService.generateKey(securityUtilService.getCurrentUserId());
        System.out.println("KEY: " + key);
        return ResponseEntity.ok(new StreamAuthorizeResponse(key));
    }

    /**
     * Prosty model odpowiedzi zwracany przez /stream/authorize.
     */
    public record StreamAuthorizeResponse(String key) {}

}
