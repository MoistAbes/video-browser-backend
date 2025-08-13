package dev.zymion.video.browser.app.repositories;

import dev.zymion.video.browser.app.models.entities.ShowEntity;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<ShowEntity, Integer> {

    @Query("SELECT DISTINCT s FROM ShowEntity s " +
            "LEFT JOIN FETCH s.seasons season " +
            "LEFT JOIN FETCH season.episodes e " +
            "WHERE s.name = :parentTitle")
    ShowEntity findByParentTitleWithSortedSeasons(@Param("parentTitle") String parentTitle);


    @Query("SELECT s.id AS id, s.name AS name, s.rootPath AS rootPath FROM ShowEntity s")
    List<ShowRootPathProjection> findAllShowsWithRootPath();

}
