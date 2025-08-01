package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.dto.VideoInfoDto;
import dev.zymion.video.browser.app.entities.VideoInfoEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoInfoMapper {

    public VideoInfoDto mapToDto(VideoInfoEntity videoInfo) {
        return VideoInfoDto.builder()
                .id(videoInfo.getId())
                .title(videoInfo.getTitle())
                .category(videoInfo.getCategory())
                .iconFileName(videoInfo.getIconFileName())
                .rootPath(videoInfo.getRootPath())
                .type(videoInfo.getType().toString())
                .build();
    }


    public List<VideoInfoDto> mapToListDto(List<VideoInfoEntity> videoInfoEntities) {
        return videoInfoEntities.stream().map(this::mapToDto).collect(Collectors.toList());
    }

}
