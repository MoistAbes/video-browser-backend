package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.models.dto.show.ShowDto;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import dev.zymion.video.browser.app.services.ShowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/show")
@Slf4j
public class ShowController {

    private final ShowService showService;

    @Autowired
    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("find/all")
    public ResponseEntity<List<ShowDto>> findAllShows() {
        List<ShowDto> result = showService.findAll();
        return ResponseEntity.ok(result);

    }

    @GetMapping("find/random")
    public ResponseEntity<List<ShowDto>> findRandomShows() {
        List<ShowDto> result = showService.findRandom();
        return ResponseEntity.ok(result);

    }

    @GetMapping("find/random/{showStructure}")
    public ResponseEntity<List<ShowDto>> findRandomShowsByStructure(@PathVariable String showStructure) {
        StructureTypeEnum type = StructureTypeEnum.safeValueOf(showStructure);
        List<ShowDto> randomShows = showService.findRandomByStructure(type);
        return ResponseEntity.ok(randomShows);
    }


    @GetMapping("find/{parentTitle}")
    public ResponseEntity<ShowDto> findShowByParentTitle(@PathVariable String parentTitle) {
        ShowDto result = showService.findByParentTitle(parentTitle);
        return ResponseEntity.ok(result);
    }

    /**
     * Okrojona lista tylko do nazwy sciezki root i id i nazwy
     * @return
     */
    @GetMapping("find/with-root-path")
    public List<ShowRootPathProjection> getShowsWithRootPath() {
        return showService.findAllShowsWithRootPath();
    }


    @PutMapping("add/genre/{showId}/{genreId}")
    public ResponseEntity<Void> addGenre(@PathVariable Long showId, @PathVariable Long genreId) {

        showService.addGenreToShow(showId, genreId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/remove/genre/{showId}/{genreId}")
    public ResponseEntity<Void> removeGenre(@PathVariable Long showId, @PathVariable Long genreId) {

        showService.removeGenreFromShow(showId, genreId);
        return ResponseEntity.ok().build();
    }

}
