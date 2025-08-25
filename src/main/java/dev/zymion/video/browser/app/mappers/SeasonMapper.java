package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.show.SeasonDto;
import dev.zymion.video.browser.app.models.entities.show.SeasonEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeasonMapper {

    private final MediaItemMapper mediaItemMapper;

    public SeasonMapper(MediaItemMapper mediaItemMapper) {
        this.mediaItemMapper = mediaItemMapper;
    }

    public SeasonDto mapToDto(SeasonEntity seasonEntity) {

        return new SeasonDto(
                seasonEntity.getId(),
                seasonEntity.getNumber(),
                mediaItemMapper.mapToDtoList(seasonEntity.getEpisodes())
        );

    }

    public List<SeasonDto> mapToDtoList(List<SeasonEntity> seasonEntities) {

        return seasonEntities.stream()
                .map(this::mapToDto)
                .toList();

    }

}
