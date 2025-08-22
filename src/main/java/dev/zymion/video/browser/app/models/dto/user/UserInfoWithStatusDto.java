package dev.zymion.video.browser.app.models.dto.user;

public record UserInfoWithStatusDto(
        Long id,
        String username,
        String iconColor,
        String icon,
        UserStatusDto status
) {}
