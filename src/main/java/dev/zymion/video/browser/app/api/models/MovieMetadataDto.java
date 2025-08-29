package dev.zymion.video.browser.app.api.models;

import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MovieMetadataDto {


    private String title;
    private String overview;
    Set<GenreEntity> genres;

}
