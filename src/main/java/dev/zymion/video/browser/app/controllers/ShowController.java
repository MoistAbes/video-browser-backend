package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.entities.ShowEntity;
import dev.zymion.video.browser.app.projections.ShowRootPathProjection;
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
    public ResponseEntity<List<ShowEntity>> findAllShows() {
        log.info("show/find/all");

        List<ShowEntity> result = showService.findAll();

        return ResponseEntity.ok(result);

    }

    @GetMapping("find/{parentTitle}")
    public ResponseEntity<ShowEntity> findShowByParentTitle(@PathVariable String parentTitle) {
        log.info("show/find/{}", parentTitle);

        ShowEntity result = showService.findByParentTitle(parentTitle);

        System.out.println("result: " + result);

        return ResponseEntity.ok(result);
    }


    @GetMapping("find/with-root-path")
    public List<ShowRootPathProjection> getShowsWithRootPath() {
        return showService.findAllShowsWithRootPath();
    }

}
