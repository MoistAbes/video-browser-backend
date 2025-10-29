package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.api.models.TmdbMovieMetadata;
import dev.zymion.video.browser.app.api.services.MovieMetadataApiService;
import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.exceptions.GenreNotFoundException;
import dev.zymion.video.browser.app.exceptions.ShowNotFoundException;
import dev.zymion.video.browser.app.models.dto.show.ShowDto;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.exceptions.ShowMappingException;
import dev.zymion.video.browser.app.mappers.ShowMapper;
import dev.zymion.video.browser.app.models.entities.show.*;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import dev.zymion.video.browser.app.repositories.show.GenreRepository;
import dev.zymion.video.browser.app.repositories.show.ShowRepository;
import dev.zymion.video.browser.app.repositories.show.ShowStructureRepository;
import dev.zymion.video.browser.app.services.util.StringUtilService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowMapper showMapper;
    private final GenreRepository genreRepository;
    private final ShowStructureRepository showStructureRepository;
    private final StringUtilService stringUtilService;
    private final MovieMetadataApiService movieMetadataApiService;
    private final ShowStructureService showStructureService;
    private final EntityManager entityManager;

    @Autowired
    public ShowService(ShowRepository showRepository, ShowMapper showMapper, GenreRepository genreRepository, ShowStructureRepository showStructureRepository, StringUtilService stringUtilService, MovieMetadataApiService movieMetadataApiService, ShowStructureService showStructureService, EntityManager entityManager) {
        this.showRepository = showRepository;
        this.showMapper = showMapper;
        this.genreRepository = genreRepository;
        this.showStructureRepository = showStructureRepository;
        this.stringUtilService = stringUtilService;
        this.movieMetadataApiService = movieMetadataApiService;
        this.showStructureService = showStructureService;
        this.entityManager = entityManager;
    }

    public List<ShowRootPathProjection> findAllShowsWithRootPath() {
        return showRepository.findAllBy();
    }


    public List<ShowEntity> setUpShows(List<MediaItemEntity> mediaItemEntities) {

        //znajdujemy wszystkie show
        List<ShowEntity> shows = showRepository.findAll();

        //zdobywamy liste nazw (będą unikalne)
        List<String> showNames = shows.stream()
                .map(ShowEntity::getName)
                .toList();


        //tworzymy mape key = showname, value lista media items tego show
        Map<String, List<MediaItemEntity>> mediaItemEntityMap = new HashMap<>();
        for (MediaItemEntity mediaItemEntity : mediaItemEntities) {
            String parentTitle = mediaItemEntity.getParentTitle();
            mediaItemEntityMap
                    .computeIfAbsent(parentTitle, k -> new ArrayList<>())
                    .add(mediaItemEntity);
        }

        //Tutaj bedziemy dodawac encje do zapisu/edycji
        List<ShowEntity> showEntityList = new ArrayList<>();

        //przechodzimy po mapie
        for (Map.Entry<String, List<MediaItemEntity>> entry : mediaItemEntityMap.entrySet()) {
            String showName = entry.getKey();

            //jesli mapa zawiera nazwe ktora wczesniej pobralismy z bazy to znaczy ze show juz istnieje i trzeba je aktualizowac
            if (showNames.contains(showName)) {
                showEntityList.add(handleShowUpdate(shows, showNames, showName, entry));
                continue;
            }


            List<MediaItemEntity> videos = entry.getValue();

//            System.out.printf("%s: %s\n", showName, videos.size());

            ShowEntity showEntity = ShowEntity.builder()
                    .movies(new ArrayList<>())
                    .name(showName)
                    .build();

            Map<Integer, SeasonEntity> seasonsEntityMap = new HashMap<>();

            for (MediaItemEntity mediaItemEntity : videos) {

                String fullPath = mediaItemEntity.getRootPath();
                int secondSlash = fullPath.indexOf("/", fullPath.indexOf("/") + 1);

                if (secondSlash != -1) {
                    showEntity.setRootPath(fullPath.substring(0, secondSlash));
                } else {
                    showEntity.setRootPath(fullPath);
                }

                if (mediaItemEntity.getType() == MediaTypeEnum.TV) {
                    try {

                        int seasonNumber = mediaItemEntity.getSeasonNumber().get();

                        // Pobierz sezon lub stwórz nowy
                        SeasonEntity seasonEntity = seasonsEntityMap.computeIfAbsent(seasonNumber, s -> SeasonEntity.builder()
                                .number(seasonNumber)
                                .episodes(new ArrayList<>())
                                .build());


                        // Utwórz odcinek i przypisz do sezonu
                        seasonEntity.getEpisodes().add(mediaItemEntity);

                    }catch (Exception e){
                        throw new ShowMappingException(
                                "Error while mapping video: " + mediaItemEntity.getTitle(), e
                        );
                    }

                }else {
                    showEntity.getMovies().add(mediaItemEntity);
                }
            }

            // Dodaj sezony do show
            showEntity.setSeasons(new ArrayList<>(seasonsEntityMap.values()));

            showEntityList.add(showEntity);

        }

        // Tutaj możesz zapisać wszystkie show do bazy
       return showRepository.saveAll(showEntityList);
    }

    private ShowEntity handleShowUpdate(List<ShowEntity> shows, List<String> showNames, String showName, Map.Entry<String, List<MediaItemEntity>> entry) {

        ShowEntity showToEdit = shows.get(showNames.indexOf(showName));

        for (MediaItemEntity mediaItemEntity : entry.getValue()) {
            if (mediaItemEntity.getType() == MediaTypeEnum.MOVIE) {
                showToEdit.getMovies().add(mediaItemEntity);
            }else if (mediaItemEntity.getType() == MediaTypeEnum.TV) {


                mediaItemEntity.getSeasonNumber().ifPresent(seasonNumber -> {
                    // szukamy istniejącego sezonu
                    Optional<SeasonEntity> existingSeasonOpt = showToEdit.getSeasons().stream()
                            .filter(seasonEntity -> seasonEntity.getNumber() == seasonNumber)
                            .findFirst();

                    SeasonEntity seasonEntity = existingSeasonOpt.orElseGet(() -> {
                        // jeśli nie ma -> tworzymy nowy sezon
                        SeasonEntity newSeason = SeasonEntity.builder()
                                .number(seasonNumber)
                                .episodes(new ArrayList<>())
                                .build();
                        showToEdit.getSeasons().add(newSeason);
                        return newSeason;
                    });

                    // dodajemy odcinek do istniejącego lub nowo utworzonego sezonu
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

    public void setUpShowsStructureType() {
        List<ShowEntity> allShows = showRepository.findAll();

        for (ShowEntity show : allShows) {
            StructureTypeEnum type = StructureTypeEnum.fromShow(show);

            ShowStructureEntity structure = showStructureRepository.findByName(type)
                    .orElseThrow(() -> new IllegalStateException("Brak zdefiniowanej struktury: " + type));

            show.setStructure(structure);
        }

        showRepository.saveAll(allShows);
    }

    public void syncShowMetadataWithTmdb(List<ShowEntity> shows) {

        List<GenreEntity> genres = genreRepository.findAll();


        for (ShowEntity show : shows) {

            //wyciagamy czysty tytuł
            String rawTitle = show.getName();
            String cleanTitle = stringUtilService.extractCleanTitle(rawTitle);
            //wyciagamy potencjalny rok produkcji
            Optional<Integer> yearOpt = stringUtilService.extractYearFromTitle(rawTitle);

            boolean isMovie = false;

            //decydujemy czy jest to film czy nie (potrzebne do fetch api tmdb inne endpointy)
            StructureTypeEnum structure = show.getStructure().getName();
            if (structure == StructureTypeEnum.SINGLE_MOVIE || structure == StructureTypeEnum.MOVIE_COLLECTION) {
                isMovie = true;
            }


            Optional<TmdbMovieMetadata> showMetadata = movieMetadataApiService.fetchMetadata(cleanTitle, yearOpt, isMovie, genres);

            //jesli znajdzie dane
            showMetadata.ifPresent(tmdbMovieMetadata -> {
                show.setGenres(tmdbMovieMetadata.getGenres());
                show.setDescription(tmdbMovieMetadata.getOverview());
            });
        }
        showRepository.saveAll(shows);

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
}
