package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.SeasonDto;
import dev.zymion.video.browser.app.models.entities.SeasonEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeasonMapper {

    private final ContentMapper contentMapper;

    public SeasonMapper(ContentMapper contentMapper) {
        this.contentMapper = contentMapper;
    }

    public SeasonDto mapToDto(SeasonEntity seasonEntity) {

        return new SeasonDto(
                seasonEntity.getId(),
                seasonEntity.getNumber(),
                contentMapper.mapToDtoList(seasonEntity.getEpisodes())
        );

    }

    public List<SeasonDto> mapToDtoList(List<SeasonEntity> seasonEntities) {

        return seasonEntities.stream()
                .map(this::mapToDto)
                .toList();

    }

}
