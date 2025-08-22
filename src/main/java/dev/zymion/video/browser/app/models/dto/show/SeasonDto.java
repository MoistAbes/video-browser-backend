package dev.zymion.video.browser.app.models.dto.show;

import java.util.List;

public record SeasonDto (
    Long id,
    int number,
    List<ContentDto> episodes
) {}
