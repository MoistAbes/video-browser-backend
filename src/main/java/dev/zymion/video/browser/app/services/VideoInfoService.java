package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.entities.VideoInfoEntity;
import dev.zymion.video.browser.app.repositories.VideoInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoInfoService {

    private final VideoInfoRepository videoInfoRepository;

    @Autowired
    public VideoInfoService(VideoInfoRepository videoInfoRepository) {
        this.videoInfoRepository = videoInfoRepository;
    }

    public VideoInfoEntity save(VideoInfoEntity videoInfoEntity) {
        return videoInfoRepository.save(videoInfoEntity);
    }

    public List<VideoInfoEntity> findAll() {
        return videoInfoRepository.findAll();
    }

    public List<VideoInfoEntity> findAllSmall() {
        return videoInfoRepository.findAllWithoutDetails();
    }

    public List<VideoInfoEntity> findAllParentTitle() {
        return videoInfoRepository.findOnePerParentTitle();
    }

    public List<VideoInfoEntity> findAllByParentTitle(String parentTitle) {
        return videoInfoRepository.findAllByParentTitle(parentTitle);
    }


}
