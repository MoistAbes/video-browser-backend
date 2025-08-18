package dev.zymion.video.browser.app.models.dto;

import dev.zymion.video.browser.app.enums.CategoryEnum;
import dev.zymion.video.browser.app.enums.GenreEnum;

import java.util.List;
import java.util.Set;

public record ShowDto (
    Long id,
    String name,
    String rootPath,
    List<SeasonDto> seasons,
    List<ContentDto> movies,
    CategoryEnum category,
    Set<GenreEnum> genres
) {}
