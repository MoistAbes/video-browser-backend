package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.services.MediaItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mediaItem")
public class MediaItemController {

    private final MediaItemService mediaItemService;


    public MediaItemController(MediaItemService mediaItemService) {
        this.mediaItemService = mediaItemService;
    }


    /**
     * Uruchamia konwersję wszystkich plików wideo, które mają nieobsługiwany kodek audio (AC3/EAC3),
     * na format AAC.
     *
     * Konwersja jest wykonywana w tle wielowątkowo, a oryginalne pliki zostają zastąpione
     * nowymi wersjami z przetworzonym audio.
     *
     * Zwraca HTTP 202 Accepted, ponieważ proces trwa asynchronicznie i nie kończy się w czasie
     * wywołania endpointa.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/convert/audio")
    public ResponseEntity<Void> convertShowsAudioCodec() throws InterruptedException {
        mediaItemService.convertMediaItemsAudioCodec();
        return ResponseEntity.accepted().build();
    }


}
