package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.config.adnotations.SkipLogging;
import dev.zymion.video.browser.app.config.properties.AppPathProperties;
import dev.zymion.video.browser.app.services.VideoService;
import dev.zymion.video.browser.app.services.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")  // dopasuj do swojego frontu
@RestController
@RequestMapping("/videos")
@Slf4j
public class VideoController {

    private final FileService fileService;
    private final RuntimeService runtimeService;
    private final AppPathProperties appPathProperties;
    private final RepositoryService repositoryService;


    @Autowired
    public VideoController(FileService fileService, RuntimeService runtimeService, AppPathProperties appPathProperties, RepositoryService repositoryService) {
        this.fileService = fileService;
        this.runtimeService = runtimeService;
        this.appPathProperties = appPathProperties;
        this.repositoryService = repositoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/scan")
    public ResponseEntity<String> scanAllVideos() {

        repositoryService.createProcessDefinitionQuery().list()
                .forEach(pd -> System.out.println(pd.getKey() + " | " + pd.getName()));

        runtimeService.startProcessInstanceByKey(
                "scan_videos_process_id",
                Map.of("videoFolder", appPathProperties.getVideoFolder().toString())
        );

        return ResponseEntity.ok("Process started");
    }

    @GetMapping("/subtitles/{subtitleTitle}")
    public ResponseEntity<Resource> getSubtitle(@RequestParam("path") String relativePath, @PathVariable("subtitleTitle") String subtitleTitle) throws IOException {
        Resource subtitles = fileService.getSubtitles(relativePath, subtitleTitle);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/vtt"))
                .body(subtitles);
    }

    @SkipLogging
    @GetMapping("/image")
    public ResponseEntity<Resource> getImageResource(@RequestParam("path") String relativePath) {
        try {
            Resource icon = fileService.getImageResource(relativePath);
            // Zgadywanie MIME typu
            String contentType = Files.probeContentType(icon.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(icon);

        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
