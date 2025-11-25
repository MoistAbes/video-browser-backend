package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.api.models.TmdbMovieMetadata;
import dev.zymion.video.browser.app.api.services.MovieMetadataApiService;
import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.exceptions.GenreNotFoundException;
import dev.zymion.video.browser.app.exceptions.ShowNotFoundException;
import dev.zymion.video.browser.app.models.dto.show.ShowDto;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.mappers.ShowMapper;
import dev.zymion.video.browser.app.models.entities.show.*;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import dev.zymion.video.browser.app.repositories.show.GenreRepository;
import dev.zymion.video.browser.app.repositories.show.ShowRepository;
import dev.zymion.video.browser.app.repositories.show.ShowStructureRepository;
import dev.zymion.video.browser.app.services.util.StringUtilService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowMapper showMapper;
    private final GenreRepository genreRepository;
    private final ShowStructureRepository showStructureRepository;
    private final StringUtilService stringUtilService;
    private final MovieMetadataApiService movieMetadataApiService;
    private final ShowStructureService showStructureService;

    @Autowired
    public ShowService(ShowRepository showRepository, ShowMapper showMapper, GenreRepository genreRepository, ShowStructureRepository showStructureRepository, StringUtilService stringUtilService, MovieMetadataApiService movieMetadataApiService, ShowStructureService showStructureService) {
        this.showRepository = showRepository;
        this.showMapper = showMapper;
        this.genreRepository = genreRepository;
        this.showStructureRepository = showStructureRepository;
        this.stringUtilService = stringUtilService;
        this.movieMetadataApiService = movieMetadataApiService;
        this.showStructureService = showStructureService;
    }

    public List<ShowRootPathProjection> findAllShowsWithRootPath() {
        return showRepository.findAllBy();
    }

    /**
     * Tworzy lub aktualizuje wszystkie show na podstawie listy MediaItemEntity
     *
     * @param mediaItemEntities lista filmów/odcinków
     * @return zapisane encje ShowEntity
     */
    public List<ShowEntity> setUpShows(List<MediaItemEntity> mediaItemEntities) {
        List<ShowEntity> shows = showRepository.findAll();
        List<String> showNames = shows.stream().map(ShowEntity::getName).toList();

        Map<String, List<MediaItemEntity>> mediaByShow = groupMediaByParentTitle(mediaItemEntities);

        List<ShowEntity> showEntityList = new ArrayList<>();
        for (Map.Entry<String, List<MediaItemEntity>> entry : mediaByShow.entrySet()) {
            String showName = entry.getKey();

            if (showNames.contains(showName)) {
                showEntityList.add(handleShowUpdate(shows, showNames, showName, entry));
            } else {
                showEntityList.add(createNewShow(showName, entry.getValue()));
            }
        }

        return showRepository.saveAll(showEntityList);
    }


    /**
     * Grupuje MediaItemEntity według parentTitle
     */
    private Map<String, List<MediaItemEntity>> groupMediaByParentTitle(List<MediaItemEntity> mediaItems) {
        return mediaItems.stream()
                .collect(Collectors.groupingBy(MediaItemEntity::getParentTitle));
    }

    /**
     * Tworzy nowe ShowEntity z listą MediaItemEntity
     */
    private ShowEntity createNewShow(String showName, List<MediaItemEntity> videos) {
        ShowEntity showEntity = ShowEntity.builder()
                .name(showName)
                .movies(new ArrayList<>())
                .build();

        Map<Integer, SeasonEntity> seasonsEntityMap = new HashMap<>();

        for (MediaItemEntity mediaItem : videos) {
            assignRootPath(showEntity, mediaItem);

            if (mediaItem.getType() == MediaTypeEnum.TV) {
                int seasonNumber = mediaItem.getSeasonNumber().orElse(0);

                SeasonEntity season = seasonsEntityMap.computeIfAbsent(seasonNumber, s -> SeasonEntity.builder()
                        .number(seasonNumber)
                        .episodes(new ArrayList<>())
                        .build());

                season.getEpisodes().add(mediaItem);
            } else {
                showEntity.getMovies().add(mediaItem);
            }
        }

        showEntity.setSeasons(new ArrayList<>(seasonsEntityMap.values()));
        return showEntity;
    }

    /**
     * Ustawia rootPath dla ShowEntity na podstawie ścieżki MediaItemEntity
     */
    private void assignRootPath(ShowEntity showEntity, MediaItemEntity mediaItem) {
        String fullPath = mediaItem.getRootPath();

        int firstSlash = fullPath.indexOf("/");
        String rootPath = firstSlash != -1
                ? fullPath.substring(0, firstSlash)
                : fullPath;

        showEntity.setRootPath(rootPath);
    }



    /**
     * Aktualizuje istniejące ShowEntity, dodając nowe filmy lub odcinki do odpowiednich sezonów
     */
    private ShowEntity handleShowUpdate(List<ShowEntity> shows,
                                        List<String> showNames,
                                        String showName,
                                        Map.Entry<String, List<MediaItemEntity>> entry) {

        ShowEntity showToEdit = shows.get(showNames.indexOf(showName));

        for (MediaItemEntity mediaItemEntity : entry.getValue()) {
            if (mediaItemEntity.getType() == MediaTypeEnum.MOVIE) {
                showToEdit.getMovies().add(mediaItemEntity);
            } else if (mediaItemEntity.getType() == MediaTypeEnum.TV) {
                mediaItemEntity.getSeasonNumber().ifPresent(seasonNumber -> {
                    Optional<SeasonEntity> existingSeasonOpt = showToEdit.getSeasons().stream()
                            .filter(seasonEntity -> seasonEntity.getNumber() == seasonNumber)
                            .findFirst();

                    SeasonEntity seasonEntity = existingSeasonOpt.orElseGet(() -> {
                        SeasonEntity newSeason = SeasonEntity.builder()
                                .number(seasonNumber)
                                .episodes(new ArrayList<>())
                                .build();
                        showToEdit.getSeasons().add(newSeason);
                        return newSeason;
                    });

                    seasonEntity.getEpisodes().add(mediaItemEntity);
                });
            }
        }

        return showToEdit;
    }

    public List<ShowDto> findAll() {
        return showMapper.mapToDtoList(showRepository.findAll());
    }

    public ShowDto findByParentTitle(String parentTitle) {
        ShowEntity showEntity = showRepository.findByParentTitle(parentTitle);

        showEntity.getSeasons().sort(Comparator.comparingInt(SeasonEntity::getNumber));

        // sortujemy odcinki w każdym sezonie po numerze odcinka w MediaItemEntity
        for (SeasonEntity seasonEntity : showEntity.getSeasons()) {
            seasonEntity.getEpisodes().sort(Comparator.comparingInt(
                    episode -> episode.getEpisodeNumber().orElse(0) // jeśli null, traktujemy jako 0
            ));
        }


        return showMapper.mapToDto(showEntity);
    }

    public List<ShowDto> findRandom() {

        int limit = 10;

        return showRepository.findRandomShows(limit)
                .stream()
                .map(showMapper::mapToDto)
                .toList();
    }

    public List<ShowDto> findRandomByStructure(StructureTypeEnum showStructureType) {

        List<ShowEntity> shows = showRepository.findRandomShowsByStructure(showStructureType.getEnumValueToString(), 9);

        return showMapper.mapToDtoList(shows);
    }

    @Transactional
    public void addGenreToShow(Long showId, Long genreId) {

        ShowEntity show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(String.valueOf(showId)));

        GenreEntity genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException(String.valueOf(genreId)));


        show.getGenres().add(genre);
        showRepository.save(show); // nie zawsze konieczne, ale bezpieczne bo transactional nad metoda
    }

    @Transactional
    public void removeGenreFromShow(Long showId, Long genreId) {

        ShowEntity show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(String.valueOf(showId)));

        show.setGenres(show.getGenres().stream()
                .filter(genre -> !genre.getId().equals(genreId))
                .collect(Collectors.toSet()));

        showRepository.save(show);
    }

    public void setUpShowsStructureType(List<Long> savedShowIds) {
        List<ShowEntity> savedShows = showRepository.findAllById(savedShowIds);
        List<ShowStructureEntity> allStructures = showStructureRepository.findAll();

        // mapa StructureTypeEnum -> ShowStructureEntity
        Map<StructureTypeEnum, ShowStructureEntity> structureMap = allStructures.stream()
                .collect(Collectors.toMap(ShowStructureEntity::getName, Function.identity()));

        for (ShowEntity show : savedShows) {
            StructureTypeEnum type = StructureTypeEnum.fromShow(show);

            ShowStructureEntity structure = structureMap.get(type);
            if (structure == null) {
                throw new IllegalStateException("Brak zdefiniowanej struktury: " + type);
            }

            show.setStructure(structure);
        }

        showRepository.saveAll(savedShows);
    }


    public void syncShowMetadataWithTmdb(List<ShowEntity> shows) {
        List<GenreEntity> genres = genreRepository.findAll();

        for (ShowEntity show : shows) {
            try {
                String rawTitle = show.getName();
                String cleanTitle = stringUtilService.extractCleanTitle(rawTitle);
                Optional<Integer> yearOpt = stringUtilService.extractYearFromTitle(rawTitle);

                StructureTypeEnum structure = show.getStructure().getName();
                boolean isMovie = structure == StructureTypeEnum.SINGLE_MOVIE
                        || structure == StructureTypeEnum.MOVIE_COLLECTION;

                Optional<TmdbMovieMetadata> showMetadata = movieMetadataApiService.fetchMetadata(cleanTitle, yearOpt, isMovie, genres);

                showMetadata.ifPresent(tmdbMovieMetadata -> {
                    show.setGenres(tmdbMovieMetadata.getGenres());
                    show.setDescription(tmdbMovieMetadata.getOverview());
                });

                // zapis pojedynczego show po pobraniu danych – bezpieczniejsze
                showRepository.save(show);

            } catch (Exception e) {
                log.error("Błąd przy synchronizacji show: " + show.getName(), e);
                // możesz zdecydować czy rzucić wyjątek czy kontynuować
            }
        }
    }


    public Map<GenreEnum, List<ShowRootPathProjection>> findRandomByStructureAndGroupedByGenre(StructureTypeEnum structureType) {

        Long structureTypeId = showStructureService.findIdByName(structureType);

        Map<GenreEnum, List<ShowRootPathProjection>> resultMap = new HashMap<>();
        List<GenreEntity> shuffledGenres = genreRepository.findAll();

        for (GenreEntity genre : shuffledGenres) {
            resultMap.computeIfAbsent(genre.getName(), k -> new ArrayList<>());
        }

        Collections.shuffle(shuffledGenres);

        if (structureTypeId != null) {
            for (GenreEntity genre : shuffledGenres) {
                List<ShowRootPathProjection> shows =
                        showRepository.findRandomShowsByStructureTypeAndGenre(structureTypeId, genre.getId(), 9);

                resultMap.put(genre.getName(), shows);

            }
        } else {
            for (GenreEntity genre : shuffledGenres) {
                List<ShowRootPathProjection> shows =
                        showRepository.findRandomShowsByGenre(genre.getId(), 9);

                resultMap.put(genre.getName(), shows);
            }
        }


        //remove from result genres with no results
        resultMap.entrySet().removeIf(entry ->
                entry.getValue() == null || entry.getValue().isEmpty()
        );


        return resultMap;
    }

    public void deleteShow(Long showId) {
        this.showRepository.deleteById(showId);
    }

    public ShowDto findById(Long showId) throws ShowNotFoundException {
        ShowEntity show = this.showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with id: " + showId));
        return showMapper.mapToDto(show);
    }

    public void deleteAllShows() {
        showRepository.deleteAll();
    }
}
