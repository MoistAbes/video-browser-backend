package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.show.ShowDto;
import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowMapper {

    private final SeasonMapper seasonMapper;
    private final ContentMapper contentMapper;

    public ShowMapper(SeasonMapper seasonMapper, ContentMapper contentMapper) {
        this.seasonMapper = seasonMapper;
        this.contentMapper = contentMapper;
    }


    public ShowDto mapToDto(ShowEntity showEntity) {

        return new ShowDto(
              showEntity.getId(),
              showEntity.getName(),
              showEntity.getRootPath(),
              seasonMapper.mapToDtoList(showEntity.getSeasons()),
              contentMapper.mapToDtoList(showEntity.getMovies()),
              showEntity.getStructure() != null ? showEntity.getStructure().getName() : null,
              showEntity.getGenres().stream()
                      .map(GenreEntity::getName)
                      .collect(Collectors.toSet())
        );

    }

    public List<ShowDto> mapToDtoList(List<ShowEntity> showEntities) {
        return showEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

}
