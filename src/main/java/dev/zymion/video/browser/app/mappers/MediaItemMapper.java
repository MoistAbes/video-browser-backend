package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.show.MediaItemDto;
import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaItemMapper {

    public MediaItemDto mapToDto(MediaItemEntity entity) {
        return new MediaItemDto(
                entity.getId(),
                entity.getTitle(),
                entity.getParentTitle(),
                entity.getSeasonNumber().orElse(null),
                entity.getEpisodeNumber().orElse(null),
                entity.getAudio().orElse(null),
                entity.getCodec().orElse(null),
                entity.getDuration(),
                entity.getType().toString(),
                entity.getFileName(),
                entity.getRootPath(),
                entity.getVideoHash()
        );
    }

    public List<MediaItemDto> mapToDtoList(List<MediaItemEntity> entities) {
        return entities.stream()
                .map(this::mapToDto)
                .toList();
    }


}
