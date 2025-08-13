package dev.zymion.video.browser.app.models.dto;

import java.util.List;

public record SeasonDto (
    Long id,
    List<ContentDto> episodes
) {}
