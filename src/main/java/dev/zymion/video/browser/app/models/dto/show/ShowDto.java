package dev.zymion.video.browser.app.models.dto.show;

import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import java.util.List;
import java.util.Set;

public record ShowDto (
    Long id,
    String name,
    String rootPath,
    List<SeasonDto> seasons,
    List<ContentDto> movies,
    StructureTypeEnum structure,
    Set<GenreEnum> genres
) {}
