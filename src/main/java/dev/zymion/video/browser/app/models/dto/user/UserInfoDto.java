package dev.zymion.video.browser.app.models.dto.user;

import java.time.LocalDate;
import java.util.List;

public record UserInfoDto(
        Long id,
        String username,
        String iconColor,
        UserIconDto icon,
        List<String> roles,
        LocalDate registrationDate
) {}
