package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.services.VideoService;
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
        try {
            videoService.scanAllVideos();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/subtitles/{subtitleTitle}")
    public ResponseEntity<Resource> getSubtitle(@RequestParam("path") String relativePath, @PathVariable("subtitleTitle") String subtitleTitle) throws IOException {
        Resource subtitles = videoService.getSubtitles(relativePath, subtitleTitle);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/vtt"))
                .body(subtitles);
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getImageResource(@RequestParam("path") String relativePath) {
        try {
            Resource icon = videoService.getImageResource(relativePath);
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

        //ToDO to moze kiedys bede uzywal
//    @GetMapping("/thumbnails")
//    public ResponseEntity<List<String>> findAllVideoInfoThumbnails(@RequestParam String rootFolderPath) {
//
//        List<String> result = videoService.getAllThumbnails(rootFolderPath);
//        return ResponseEntity.ok(result);
//    }

}
