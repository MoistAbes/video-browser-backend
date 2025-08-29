package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenreMapper {

    public GenreDto mapToDto(GenreEntity genreEntity) {
        return new GenreDto(
                genreEntity.getId(),
                genreEntity.getName().name()
        );
    }

    public Set<GenreDto> mapToDtoSet(Set<GenreEntity> genreEntitySet) {
        return genreEntitySet.stream()
                .map(this::mapToDto)
                .collect(Collectors.toSet());
    }

    public List<GenreDto> mapToDtoList(List<GenreEntity> genreEntitylist) {
        return genreEntitylist.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

}
