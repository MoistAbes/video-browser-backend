package dev.zymion.video.browser.app.api.services;


import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.models.dto.show.GenreListWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
public class GenreCache {

    private final RestTemplate restTemplate;
    @Value("${tmdb.api.key}")
    private String apiKey;
    private final Map<Integer, String> genreMap = new HashMap<>();

    public GenreCache(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @PostConstruct
    public void init() {
        String url = String.format("https://api.themoviedb.org/3/genre/movie/list?api_key=%s", apiKey);
        ResponseEntity<GenreListWrapper> response = restTemplate.getForEntity(url, GenreListWrapper.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            for (GenreDto genre : response.getBody().genres()) {
                genreMap.put(genre.id().intValue(), genre.name());
            }
        }
    }

    public List<String> getGenreNames(List<Integer> ids) {
        return ids.stream()
                .map(genreMap::get)
                .filter(Objects::nonNull)
                .toList();
    }
}


