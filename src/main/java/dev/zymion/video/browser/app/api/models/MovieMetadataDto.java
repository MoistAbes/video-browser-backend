package dev.zymion.video.browser.app.api.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MovieMetadataDto {


    private String title;
    private String overview;
    List<String> genreNames;

}
