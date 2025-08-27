package dev.zymion.video.browser.app.api.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbSearchResponse {

    private List<TmdbMovieResult> results;

}
