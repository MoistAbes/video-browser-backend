package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.show.ContentDto;
import dev.zymion.video.browser.app.models.entities.show.ContentEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentMapper {

    private final MediaItemMapper mediaItemMapper;

    public ContentMapper(MediaItemMapper mediaItemMapper) {
        this.mediaItemMapper = mediaItemMapper;
    }

    public ContentDto mapToDto(ContentEntity contentEntity) {

        return new ContentDto(
                contentEntity.getId(),
                contentEntity.getType().toString(),
                mediaItemMapper.mapToDto(contentEntity.getMediaItem())

        );
    }

    public List<ContentDto> mapToDtoList(List<ContentEntity> contentEntities) {

        return contentEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

}
