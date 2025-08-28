package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.services.GenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/genre")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/find/all")
    public ResponseEntity<List<GenreDto>> findAllGenres() {
        return ResponseEntity.ok(genreService.findAllGenres());
    }

    @GetMapping("/find/all/names")
    public ResponseEntity<List<String>> findAllGenreNames() {
        return ResponseEntity.ok(genreService.findAllGenresNames());
    }

}
