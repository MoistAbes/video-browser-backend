package dev.zymion.video.browser.app.models.dto.show;

import dev.zymion.video.browser.app.enums.GenreEnum;

public record GenreDto(
        Long id,
        GenreEnum name
) {}
