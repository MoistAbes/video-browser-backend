package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.services.VideoService;
import dev.zymion.video.browser.app.services.helper.AppPathProperties;
import dev.zymion.video.browser.app.services.helper.FFprobeHelper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")  // dopasuj do swojego frontu
@RestController
@RequestMapping("/videos")
@Slf4j
public class VideoController {

    private final VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }



    @GetMapping("/scan")
    public ResponseEntity<Void> scanAllVideos() {
        log.info("videos/scan Scanning all videos");

        try {
            videoService.scanAllVideos();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }



    @GetMapping("/subtitles/{subtitleTitle}")
    public ResponseEntity<Resource> getSubtitle(@RequestParam("path") String relativePath, @PathVariable("subtitleTitle") String subtitleTitle) throws IOException {
        log.info("videos/subtitles/{}: ", subtitleTitle);


        Resource subtitles = videoService.getSubtitles(relativePath, subtitleTitle);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/vtt"))
                .body(subtitles);
    }



    //    ToDO niby tutaj icon jest ale tym moge pobrac jakikolwiek img jest zarzuce dobra sciezke
    @GetMapping("/icon")
    public ResponseEntity<Resource> getVideoIcon(@RequestParam("path") String relativePath) {
        log.info("videos/icon/{}: ", relativePath);

        try {
            Resource icon = videoService.getVideoIcon(relativePath);
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


    @GetMapping("/thumbnails")
    public ResponseEntity<List<String>> findAllVideoInfoThumbnails(@RequestParam String rootFolderPath) {
        log.info("Find all video info thumbnails");

        List<String> result = videoService.getAllThumbnails(rootFolderPath);
        return ResponseEntity.ok(result);
    }

}
