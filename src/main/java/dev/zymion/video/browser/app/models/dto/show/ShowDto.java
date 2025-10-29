package dev.zymion.video.browser.app.models.dto.show;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import java.util.List;
import java.util.Set;

public record ShowDto (
    Long id,
    String name,
    String rootPath,
    String description,
    List<SeasonDto> seasons,
    List<MediaItemDto> movies,
    StructureTypeEnum structure,
    Set<GenreDto> genres
) {}
