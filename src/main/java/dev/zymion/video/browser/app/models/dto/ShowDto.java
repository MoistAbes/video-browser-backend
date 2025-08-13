package dev.zymion.video.browser.app.models.dto;

import java.util.List;

public record ShowDto (
    Long id,
    String name,
    String rootPath,
    List<SeasonDto> seasons,
    List<ContentDto> movies
) {}
