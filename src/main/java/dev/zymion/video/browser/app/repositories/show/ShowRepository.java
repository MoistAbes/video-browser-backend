package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<ShowEntity, Integer> {


    @Query("SELECT s FROM ShowEntity s WHERE s.name = :parentTitle")
    ShowEntity findByParentTitle(@Param("parentTitle") String parentTitle);


    @Query("SELECT s.id AS id, s.name AS name, s.rootPath AS rootPath FROM ShowEntity s")
    List<ShowRootPathProjection> findAllShowsWithRootPath();

    @Query(value = "SELECT * FROM shows ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<ShowEntity> findRandomShows(@Param("limit") int limit);

}
