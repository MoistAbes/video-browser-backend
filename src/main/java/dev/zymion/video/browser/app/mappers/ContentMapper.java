package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.ContentDto;
import dev.zymion.video.browser.app.models.entities.ContentEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Set<ContentDto> mapToDtoSet(Set<ContentEntity> contentEntities) {

        return contentEntities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toSet());
    }

    public List<ContentDto> mapToDtoList(List<ContentEntity> contentEntities) {

        return contentEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

}
