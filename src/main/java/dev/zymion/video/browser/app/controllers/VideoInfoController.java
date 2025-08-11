package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.dto.VideoInfoDto;
import dev.zymion.video.browser.app.entities.VideoInfoEntity;
import dev.zymion.video.browser.app.mappers.VideoInfoMapper;
import dev.zymion.video.browser.app.services.VideoInfoService;
import dev.zymion.video.browser.app.services.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/video-info")
@Slf4j
public class VideoInfoController {

    private final VideoInfoService videoInfoService;
    private final VideoInfoMapper videoInfoMapper;
    private final VideoService videoService;

    @Autowired
    public VideoInfoController(VideoInfoService videoInfoService, VideoInfoMapper videoInfoMapper, VideoService videoService) {
        this.videoInfoService = videoInfoService;
        this.videoInfoMapper = videoInfoMapper;
        this.videoService = videoService;
    }

    @GetMapping("find-all")
    public ResponseEntity<List<VideoInfoEntity>> findAllVideoInfo() {
        log.info("video-info/find-all");

        List<VideoInfoEntity> result = videoInfoService.findAll();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("find-all-small")
    public ResponseEntity<List<VideoInfoDto>> findAllVideoInfoSmall() {
        log.info("video-info/find-all-small");

        List<VideoInfoEntity> result = videoInfoService.findAllSmall();
        List<VideoInfoDto> mappedResult = videoInfoMapper.mapToListDto(result);

        return new ResponseEntity<>(mappedResult, HttpStatus.OK);
    }

    @GetMapping("find-all/parent-title")
    public ResponseEntity<List<VideoInfoDto>> findAllVideoInfoParentTitle() {
        log.info("video-info/find-all/parent-title");

        List<VideoInfoEntity> result = videoInfoService.findAllParentTitle();

        //ToDO to bedzie trzeba jakos inaczej zalatwic ale poki co niech tak bedzie
        //dziwne to jest
        for (VideoInfoEntity videoInfoEntity : result) {
            videoInfoEntity.setTitle(videoInfoEntity.getVideoDetails().getParentTitle());
        }

        return new ResponseEntity<>(videoInfoMapper.mapToListDto(result), HttpStatus.OK);
    }

    @GetMapping("/find-all-by/parent-title")
    public ResponseEntity<List<VideoInfoEntity>> findAllByParentTitle(@RequestParam String parentTitle) {
        log.info("video-info/find-all-by/parent-title");


        List<VideoInfoEntity> result = videoInfoService.findAllByParentTitle(parentTitle);

        System.out.println(result.size());

        return ResponseEntity.ok(result);
    }



}
