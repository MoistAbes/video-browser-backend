package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.api.models.TmdbGenreModel;
import dev.zymion.video.browser.app.api.services.MovieMetadataApiService;
import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.mappers.GenreMapper;
import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import dev.zymion.video.browser.app.repositories.show.GenreRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;
    private final MovieMetadataApiService movieMetadataApiService;

    public GenreService(GenreRepository genreRepository, GenreMapper genreMapper, MovieMetadataApiService movieMetadataApiService) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
        this.movieMetadataApiService = movieMetadataApiService;
    }

    public List<String> findAllGenresNames() {
        return genreRepository.findAllGenreNames();
    }

    public List<GenreDto> findAllGenres() {
        return genreMapper.mapToDtoList(genreRepository.findAll());
    }


    public void updateGenresFromTmdbApi() {

//        genreRepository.deleteAll();

        List<TmdbGenreModel> genres = movieMetadataApiService.fetchAllGenres();

        Map<GenreEnum, GenreEntity> genreMap = new HashMap<>();

        for (TmdbGenreModel genre : genres) {
            GenreEnum genreName = GenreEnum.fromTmdbName(genre.getName());
            GenreEntity existing = genreMap.get(genreName);

            if (existing == null) {
                // Tworzymy nowy wpis jeśli jeszcze nie istnieje
                GenreEntity.GenreEntityBuilder builder = GenreEntity.builder()
                        .name(genreName);

                if (genre.getMediaType().equals(MediaTypeEnum.MOVIE)) {
                    builder.tmdbMovieGenreId(genre.getId());
                } else if (genre.getMediaType().equals(MediaTypeEnum.TV)) {
                    builder.tmdbTvGenreId(genre.getId());
                }

                genreMap.put(genreName, builder.build());
            } else {
                // Uzupełniamy istniejący wpis, jeśli brakowało ID
                if (genre.getMediaType().equals(MediaTypeEnum.MOVIE)) {
                    existing.setTmdbMovieGenreId(genre.getId());
                } else if (genre.getMediaType().equals(MediaTypeEnum.TV)) {
                    existing.setTmdbTvGenreId(genre.getId());
                }
            }
        }

        List<GenreEntity> newGenreList = new ArrayList<>(genreMap.values());

        genreRepository.saveAll(newGenreList);

    }



}
