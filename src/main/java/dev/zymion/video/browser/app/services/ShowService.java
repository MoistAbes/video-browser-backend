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
                                .episodes(new ArrayList<>())
                                .build());

                        ContentEntity contentEntity = ContentEntity.builder()
                                .type(MediaTypeEnum.EPISODE)
                                .mediaItem(mediaItemEntity)
                                .season(seasonEntity)
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
                            .show(showEntity)
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

    public List<ShowDto> findAll() {
        return showMapper.mapToDtoList(showRepository.findAll());
    }

    public ShowDto findByParentTitle(String parentTitle) {


//        ShowEntity showEntity = showRepository.findByParentTitleWithSortedSeasons(parentTitle);

        //ToDO tutaj by sie przydalo posortowac

        return showMapper.mapToDto(showRepository.findByParentTitleWithSortedSeasons(parentTitle));
    }
}
