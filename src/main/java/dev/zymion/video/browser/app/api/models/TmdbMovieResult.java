package dev.zymion.video.browser.app.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbMovieResult {


    private String title; // dla filmów
    private String name;  // dla seriali

    private String overview;

    @JsonProperty("genre_ids")
    private List<Long> genreIds;


    public String getResolvedTitle(boolean isMovie) {
        return isMovie ? title : name;
    }


}
