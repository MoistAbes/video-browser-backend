package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.mappers.GenreMapper;
import dev.zymion.video.browser.app.models.dto.show.GenreDto;
import dev.zymion.video.browser.app.repositories.show.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public GenreService(GenreRepository genreRepository, GenreMapper genreMapper) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }


    public List<String> findAllGenresNames() {
        return genreRepository.findAllGenreNames();
    }

    public List<GenreDto> findAllGenres() {
        return genreMapper.mapToDtoList(genreRepository.findAll());
    }

}
