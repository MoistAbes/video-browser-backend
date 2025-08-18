package dev.zymion.video.browser.app.models.dto;

import java.util.List;

public record SeasonDto (
    Long id,
    int number,
    List<ContentDto> episodes
) {}
