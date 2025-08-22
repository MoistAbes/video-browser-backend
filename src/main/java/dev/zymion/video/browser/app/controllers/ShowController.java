package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.dto.show.ShowDto;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import dev.zymion.video.browser.app.services.ShowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
