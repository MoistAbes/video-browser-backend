package dev.zymion.video.browser.app.models.dto.user;

public record UserInfoDto(
        Long id,
        String username,
        String iconColor,
        UserIconDto icon
) {}
