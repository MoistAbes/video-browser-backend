package dev.zymion.video.browser.app.repositories;

import dev.zymion.video.browser.app.entities.ShowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<ShowEntity, Integer> {

    @Query("SELECT DISTINCT s FROM ShowEntity s " +
            "LEFT JOIN FETCH s.seasons season " +
            "LEFT JOIN FETCH season.episodes e " +
            "WHERE s.name = :parentTitle")
    ShowEntity findByParentTitleWithSortedSeasons(@Param("parentTitle") String parentTitle);


}
