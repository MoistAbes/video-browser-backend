package dev.zymion.video.browser.app.api.services;

import dev.zymion.video.browser.app.api.models.*;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieMetadataApiService {

    private final RestTemplate restTemplate;
    @Value("${tmdb.api.key}")
    private String apiKey;

    public MovieMetadataApiService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }


    public Optional<TmdbMovieMetadata> fetchMetadata(String title, Optional<Integer> year, boolean isMovie, List<GenreEntity> genres) {

        log.info("Fetching movie metadata for title: {} and year: {}", title, year.isPresent() ? year.get() : "not found");

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

        ResponseEntity<TmdbSearchResponse> response = restTemplate.getForEntity(url, TmdbSearchResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().getResults().isEmpty()) {
            TmdbMovieResult result = response.getBody().getResults().getFirst();

            String resolvedTitle = result.getResolvedTitle(isMovie);

            Set<GenreEntity> matchingGenres = genres.stream()
                    .filter(genreEntity -> result.getGenreIds().stream()
                            .anyMatch(tmdbId ->
                                    (genreEntity.getTmdbMovieGenreId() != null && genreEntity.getTmdbMovieGenreId().equals(tmdbId)) ||
                                            (genreEntity.getTmdbTvGenreId() != null && genreEntity.getTmdbTvGenreId().equals(tmdbId))
                            )
                    )
                    .collect(Collectors.toSet());

            TmdbMovieMetadata tmdbMovieMetadata = new TmdbMovieMetadata(resolvedTitle, result.getOverview(), matchingGenres);

            log.info("Movie metadata: {}", tmdbMovieMetadata);

            return Optional.of(tmdbMovieMetadata);
        }

        log.info("No movie metadata found");
        return Optional.empty();
    }


    public List<TmdbGenreModel> fetchAllGenres() {

        List<TmdbGenreModel> genreList = new ArrayList<>();

        String movieGenresUrl = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey + "&language=en-EN";
        String tvGenresUrl = "https://api.themoviedb.org/3/genre/tv/list?api_key=" + apiKey + "&language=en-EN";

        ResponseEntity<TmdbGenreResponse> movieResponse = restTemplate.getForEntity(movieGenresUrl, TmdbGenreResponse.class);
        ResponseEntity<TmdbGenreResponse> tvResponse = restTemplate.getForEntity(tvGenresUrl, TmdbGenreResponse.class);

        if (movieResponse.getStatusCode() == HttpStatus.OK && movieResponse.getBody() != null) {

            for (TmdbGenreModel genre : movieResponse.getBody().getGenres()) {
                genre.setMediaType(MediaTypeEnum.MOVIE);
                genreList.add(genre);
            }
        }

        if (tvResponse.getStatusCode() == HttpStatus.OK && tvResponse.getBody() != null) {

            for (TmdbGenreModel genre : tvResponse.getBody().getGenres()) {
                genre.setMediaType(MediaTypeEnum.TV);
                genreList.add(genre);
            }
        }

        //

        return genreList;
    }





}
