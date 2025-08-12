package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.entities.*;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import dev.zymion.video.browser.app.enums.VideoTypeEnum;
import dev.zymion.video.browser.app.exceptions.ShowMappingException;
import dev.zymion.video.browser.app.projections.ShowRootPathProjection;
import dev.zymion.video.browser.app.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShowService {

    private final ShowRepository showRepository;

    @Autowired
    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public List<ShowRootPathProjection> findAllShowsWithRootPath() {
        return showRepository.findAllShowsWithRootPath();
    }


    public void setUpShows2(List<MediaItemEntity> mediaItemEntities) {
        // Grupowanie po parentTitle
        Map<String, List<MediaItemEntity>> mediaItemEntityMap = new HashMap<>();
        for (MediaItemEntity mediaItemEntity : mediaItemEntities) {
            String parentTitle = mediaItemEntity.getParentTitle();
            mediaItemEntityMap
                    .computeIfAbsent(parentTitle, k -> new ArrayList<>())
                    .add(mediaItemEntity);
        }

        List<ShowEntity> showEntityList = new ArrayList<>();

        for (Map.Entry<String, List<MediaItemEntity>> entry : mediaItemEntityMap.entrySet()) {
            String showName = entry.getKey();
            List<MediaItemEntity> videos = entry.getValue();

//            System.out.printf("%s: %s\n", showName, videos.size());

            ShowEntity showEntity = ShowEntity.builder()
                    .movies(new ArrayList<>())
                    .name(showName)
                    .build();

            Map<Integer, SeasonEntity> seasonsEntityMap = new HashMap<>();

            for (MediaItemEntity mediaItemEntity : videos) {

                showEntity.setRootPath(mediaItemEntity.getRootPath());

                if (mediaItemEntity.getType() == MediaTypeEnum.EPISODE) {
                    try {

                        int seasonNumber = mediaItemEntity.getSeasonNumber().get();

                        // Pobierz sezon lub stwórz nowy
                        SeasonEntity seasonEntity = seasonsEntityMap.computeIfAbsent(seasonNumber, s -> SeasonEntity.builder()
                                .show(showEntity)
                                .episodes(new HashSet<>())
                                .build());

                        // Utwórz odcinek i przypisz do sezonu
                        EpisodeEntity episodeEntity = EpisodeEntity.builder()
                                .season(seasonEntity)
                                .mediaItem(mediaItemEntity)
                                .build();

                        seasonEntity.getEpisodes().add(episodeEntity);


                    }catch (Exception e){
                        throw new ShowMappingException(
                                "Error while mapping video: " + mediaItemEntity.getTitle(), e
                        );
                    }

                }else {

                    MovieEntity movieEntity = MovieEntity.builder()
                            .show(showEntity)
                            .mediaItem(mediaItemEntity)
                            .build();

                    showEntity.getMovies().add(movieEntity);

                }
            }

            // Dodaj sezony do show
            showEntity.setSeasons(new HashSet<>(seasonsEntityMap.values()));

            showEntityList.add(showEntity);
        }

        // Tutaj możesz zapisać wszystkie show do bazy
        showRepository.saveAll(showEntityList);
    }

    public List<ShowEntity> findAll() {

        return showRepository.findAll();
    }

    public ShowEntity findByParentTitle(String parentTitle) {

        return showRepository.findByParentTitleWithSortedSeasons(parentTitle);

    }
}
