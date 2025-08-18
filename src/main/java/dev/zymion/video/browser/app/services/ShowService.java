package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.models.dto.ShowDto;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.exceptions.ShowMappingException;
import dev.zymion.video.browser.app.mappers.ShowMapper;
import dev.zymion.video.browser.app.models.entities.ContentEntity;
import dev.zymion.video.browser.app.models.entities.MediaItemEntity;
import dev.zymion.video.browser.app.models.entities.SeasonEntity;
import dev.zymion.video.browser.app.models.entities.ShowEntity;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import dev.zymion.video.browser.app.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowMapper showMapper;

    @Autowired
    public ShowService(ShowRepository showRepository, ShowMapper showMapper) {
        this.showRepository = showRepository;
        this.showMapper = showMapper;
    }

    public List<ShowRootPathProjection> findAllShowsWithRootPath() {
        return showRepository.findAllShowsWithRootPath();
    }


    public void setUpShows(List<MediaItemEntity> mediaItemEntities) {


        List<ShowEntity> shows = showRepository.findAll();
        List<String> showNames = shows.stream()
                .map(ShowEntity::getName)
                .toList();


        // Grupowanie po parentTitle
        Map<String, List<MediaItemEntity>> mediaItemEntityMap = new HashMap<>();
        for (MediaItemEntity mediaItemEntity : mediaItemEntities) {
            String parentTitle = mediaItemEntity.getParentTitle();
            mediaItemEntityMap
                    .computeIfAbsent(parentTitle, k -> new ArrayList<>())
                    .add(mediaItemEntity);
        }

        //Tutaj bedziemy dodawac encje do zapisu/edycji
        List<ShowEntity> showEntityList = new ArrayList<>();

        for (Map.Entry<String, List<MediaItemEntity>> entry : mediaItemEntityMap.entrySet()) {
            String showName = entry.getKey();

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



                if (mediaItemEntity.getType() == MediaTypeEnum.EPISODE) {
                    try {

                        int seasonNumber = mediaItemEntity.getSeasonNumber().get();

                        // Pobierz sezon lub stwórz nowy
                        SeasonEntity seasonEntity = seasonsEntityMap.computeIfAbsent(seasonNumber, s -> SeasonEntity.builder()
                                .number(seasonNumber)
                                .episodes(new ArrayList<>())
                                .build());

                        ContentEntity contentEntity = ContentEntity.builder()
                                .type(MediaTypeEnum.EPISODE)
                                .mediaItem(mediaItemEntity)
                                .build();

                        // Utwórz odcinek i przypisz do sezonu
                        seasonEntity.getEpisodes().add(contentEntity);

                    }catch (Exception e){
                        throw new ShowMappingException(
                                "Error while mapping video: " + mediaItemEntity.getTitle(), e
                        );
                    }

                }else {

                    ContentEntity contentEntity = ContentEntity.builder()
                            .mediaItem(mediaItemEntity)
                            .type(MediaTypeEnum.MOVIE)
                            .build();


                    showEntity.getMovies().add(contentEntity);
                }
            }

            // Dodaj sezony do show
            showEntity.setSeasons(new ArrayList<>(seasonsEntityMap.values()));

            showEntityList.add(showEntity);

        }

        // Tutaj możesz zapisać wszystkie show do bazy
        showRepository.saveAll(showEntityList);
    }

    private ShowEntity handleShowUpdate(List<ShowEntity> shows, List<String> showNames, String showName, Map.Entry<String, List<MediaItemEntity>> entry) {

        ShowEntity showToEdit = shows.get(showNames.indexOf(showName));

        for (MediaItemEntity mediaItemEntity : entry.getValue()) {
            if (mediaItemEntity.getType() == MediaTypeEnum.MOVIE) {
                showToEdit.getMovies().add(ContentEntity.builder()
                        .type(MediaTypeEnum.MOVIE)
                        .mediaItem(mediaItemEntity)
                        .build());
            }else if (mediaItemEntity.getType() == MediaTypeEnum.EPISODE) {


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
                    seasonEntity.getEpisodes().add(ContentEntity.builder()
                            .mediaItem(mediaItemEntity)
                            .type(MediaTypeEnum.EPISODE)
                            .build());
                });

            }
        }

        return showToEdit;

//        showEntityList.add(showToEdit);


    }

    public List<ShowDto> findAll() {
        return showMapper.mapToDtoList(showRepository.findAll());
    }

    public ShowDto findByParentTitle(String parentTitle) {
        ShowEntity showEntity = showRepository.findByParentTitle(parentTitle);

        showEntity.getSeasons().sort(Comparator.comparingInt(SeasonEntity::getNumber));

        return showMapper.mapToDto(showEntity);
    }


    public List<ShowDto> findRandom() {

        int limit = 10;

        return showRepository.findRandomShows(limit)
                .stream()
                .map(showMapper::mapToDto)
                .toList();
    }
}
