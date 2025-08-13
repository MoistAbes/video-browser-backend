package dev.zymion.video.browser.app.models.dto;

public record MediaItemDto (
        Long id,
        String title,
        Integer seasonNumber,
        Integer episodeNumber,
        String type,
        String fileName
) {}
