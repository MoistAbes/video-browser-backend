package dev.zymion.video.browser.app.api.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TmdbGenreResponse {

    private List<TmdbGenreModel> genres;

}
