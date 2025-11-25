package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.services.GenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/genre")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/find/all")
    public ResponseEntity<List<GenreDto>> findAllGenres(@RequestParam(required = false) Boolean shuffle) {
        List<GenreDto> genres = genreService.findAllGenres();
        if (Boolean.TRUE.equals(shuffle)) {
            Collections.shuffle(genres);
        }
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/find/all/names")
    public ResponseEntity<List<String>> findAllGenreNames() {
        return ResponseEntity.ok(genreService.findAllGenresNames());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public ResponseEntity<Void> updateGenres() {

        genreService.updateGenresFromTmdbApi();

        return ResponseEntity.ok().build();
    }


}
