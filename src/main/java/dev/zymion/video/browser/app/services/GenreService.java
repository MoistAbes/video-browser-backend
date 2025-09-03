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

        genreRepository.deleteAll();

        List<TmdbGenreModel> genres = movieMetadataApiService.fetchAllGenres();

        List<GenreEntity> newGenreList = new ArrayList<>();


        for (TmdbGenreModel genre : genres) {
             newGenreList.add(GenreEntity.builder()
                             .id(genre.getId())
                             .name(GenreEnum.fromTmdbName(genre.getName()))
                             .mediaType(genre.getMediaType())
                     .build());
        }
        genreRepository.saveAll(newGenreList);

    }



}
