package dev.zymion.video.browser.app.models.dto.user;

public record UserStatusDto(
        Long id,
        String videoTitle,
        boolean isOnline,
        boolean isWatching
) {}
