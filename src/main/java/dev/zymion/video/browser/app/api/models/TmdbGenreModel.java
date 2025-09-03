package dev.zymion.video.browser.app.api.models;

import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class TmdbGenreModel {

    private Long id;
    private String name;
    private MediaTypeEnum mediaType;
}
