package dev.zymion.video.browser.app.api.services;

import dev.zymion.video.browser.app.api.models.MovieMetadataDto;
import dev.zymion.video.browser.app.api.models.TmdbMovieResult;
import dev.zymion.video.browser.app.api.models.TmdbSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Optional;

@Service
public class MovieMetadataApiService {

    private final RestTemplate restTemplate;
    private final GenreCache genreCache;
    @Value("${tmdb.api.key}")
    private String apiKey;

    public MovieMetadataApiService(RestTemplateBuilder builder, GenreCache genreCache) {
        this.restTemplate = builder.build();
        this.genreCache = genreCache;
    }


    public Optional<MovieMetadataDto> fetchMetadata(String title, Optional<Integer> year, boolean isMovie) {
        String baseApiUrl = "https://api.themoviedb.org/3/search/" + (isMovie ? "movie" : "tv");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseApiUrl)
                .queryParam("api_key", apiKey)
                .queryParam("query", title);

        if (year.isPresent()) {
            if (isMovie) {
                builder.queryParam("year", year.get());
            } else {
                builder.queryParam("first_air_date_year", year.get());
            }
        }

        String url = builder.toUriString();
        System.out.println("url: " + url);

        ResponseEntity<TmdbSearchResponse> response = restTemplate.getForEntity(url, TmdbSearchResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().getResults().isEmpty()) {
            TmdbMovieResult result = response.getBody().getResults().getFirst();

            String resolvedTitle = result.getResolvedTitle(isMovie);
            List<String> genreNames = genreCache.getGenreNames(result.getGenreIds());

            return Optional.of(new MovieMetadataDto(resolvedTitle, result.getOverview(), genreNames));
        }

        return Optional.empty();
    }





}
