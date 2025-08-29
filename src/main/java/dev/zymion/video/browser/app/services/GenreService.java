package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.mappers.GenreMapper;
import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.models.dto.show.GenreListWrapper;
import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import dev.zymion.video.browser.app.repositories.show.GenreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {


    private final RestTemplate restTemplate;
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Value("${tmdb.api.key}")
    private String apiKey;


    public GenreService(RestTemplateBuilder restTemplateBuilder, GenreRepository genreRepository, GenreMapper genreMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }


    public List<String> findAllGenresNames() {
        return genreRepository.findAllGenreNames();
    }

    public List<GenreDto> findAllGenres() {
        return genreMapper.mapToDtoList(genreRepository.findAll());
    }


    public void updateGenresFromTmdb() {
        updateGenresFromEndpoint("https://api.themoviedb.org/3/genre/movie/list");
        updateGenresFromEndpoint("https://api.themoviedb.org/3/genre/tv/list");
    }

    private void updateGenresFromEndpoint(String endpoint) {
        String url = String.format("%s?api_key=%s", endpoint, apiKey);
        ResponseEntity<GenreListWrapper> response = restTemplate.getForEntity(url, GenreListWrapper.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            for (GenreDto genre : response.getBody().genres()) {
                genreRepository.findById(genre.id())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.setName(GenreEnum.fromTmdbName(genre.name()));
                                    genreRepository.save(existing);
                                },
                                () -> genreRepository.save(new GenreEntity(genre.id(), GenreEnum.fromTmdbName(genre.name())))
                        );
            }
        }
    }



}
