package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.entities.*;
import dev.zymion.video.browser.app.enums.VideoTypeEnum;
import dev.zymion.video.browser.app.exceptions.ShowMappingException;
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


    public void setUpShows(List<VideoInfoEntity> videoInfoEntityList) {



        // Grupowanie po parentTitle
        Map<String, List<VideoInfoEntity>> videoInfoEntityMap = new HashMap<>();

        for (VideoInfoEntity videoInfoEntity : videoInfoEntityList) {
            String parentTitle = videoInfoEntity.getVideoDetails().getParentTitle();
            videoInfoEntityMap
                    .computeIfAbsent(parentTitle, k -> new ArrayList<>())
                    .add(videoInfoEntity);
        }

        List<ShowEntity> showEntityList = new ArrayList<>();

        for (Map.Entry<String, List<VideoInfoEntity>> entry : videoInfoEntityMap.entrySet()) {
            String showName = entry.getKey();
            List<VideoInfoEntity> videos = entry.getValue();

//            System.out.printf("%s: %s\n", showName, videos.size());

            ShowEntity showEntity = ShowEntity.builder()
                    .movies(new ArrayList<>())
                    .name(showName)
                    .build();

            Map<Integer, SeasonEntity> seasonsEntityMap = new HashMap<>();

            for (VideoInfoEntity videoInfoEntity : videos) {

                if (isSeason(videoInfoEntity)) {
                    try {

                        int seasonNumber = videoInfoEntity.getVideoDetails().getSeason();

                        // Pobierz sezon lub stwórz nowy
                        SeasonEntity seasonEntity = seasonsEntityMap.computeIfAbsent(seasonNumber, s -> {
                            SeasonEntity season = SeasonEntity.builder()
                                    .show(showEntity)
                                    .number(seasonNumber)
                                    .episodes(new HashSet<>())
                                    .build();
                            return season;
                        });

                        // Utwórz odcinek i przypisz do sezonu
                        EpisodeEntity episodeEntity = EpisodeEntity.builder()
                                .season(seasonEntity)
                                .number(videoInfoEntity.getVideoDetails().getEpisode())
                                .videoInfo(videoInfoEntity)
                                .build();

                        seasonEntity.getEpisodes().add(episodeEntity);


                    }catch (Exception e){
                        throw new ShowMappingException(
                                "Error while mapping video: " + videoInfoEntity.getTitle(), e
                        );
                    }

                }else {

                    MovieEntity movieEntity = MovieEntity.builder()
                            .show(showEntity)
                            .videoInfo(videoInfoEntity)
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


    private boolean isSeason(VideoInfoEntity videoInfoEntity) {
        return (videoInfoEntity.getType().equals(VideoTypeEnum.SHOW) ||
                videoInfoEntity.getType().equals(VideoTypeEnum.ANIME))
                && videoInfoEntity.getVideoDetails().getSeason() != null;
    }


    public List<ShowEntity> findAll() {

        return showRepository.findAll();
    }

    public ShowEntity findByParentTitle(String parentTitle) {

        return showRepository.findByParentTitleWithSortedSeasons(parentTitle);

    }
}
