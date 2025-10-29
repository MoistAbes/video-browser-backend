package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import dev.zymion.video.browser.app.models.projections.ShowRootPathProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<ShowEntity, Long> {


    @Query("SELECT s FROM ShowEntity s WHERE s.name = :parentTitle")
    ShowEntity findByParentTitle(@Param("parentTitle") String parentTitle);

    List<ShowRootPathProjection> findAllBy();

    @Query(value = "SELECT * FROM shows ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<ShowEntity> findRandomShows(@Param("limit") int limit);

    @Query(value = """
    SELECT s.* 
    FROM shows s
    JOIN show_structures ss ON s.show_structure_id = ss.id
    WHERE ss.name = :structureType
    ORDER BY RANDOM()
    LIMIT :limit
    """, nativeQuery = true)
    List<ShowEntity> findRandomShowsByStructure(
            @Param("structureType") String structureType,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT s.* 
    FROM shows s
    JOIN shows_genres sg ON s.id = sg.show_id
    WHERE sg.genre_id = :genreId
    ORDER BY RANDOM()
    LIMIT :limit
    """, nativeQuery = true)
    List<ShowRootPathProjection> findRandomShowsByGenre(@Param("genreId") Long genreId, @Param("limit") int limit);



    @Query(value = """
    SELECT s.*
    FROM shows s
    WHERE s.show_structure_id = :structureTypeId
      AND s.id IN (
          SELECT sg.show_id
          FROM shows_genres sg
          WHERE sg.genre_id = :genreId
      )
    ORDER BY RANDOM()
    LIMIT :limit
    """, nativeQuery = true)
    List<ShowRootPathProjection> findRandomShowsByStructureTypeAndGenre(
            @Param("structureTypeId") Long structureTypeId,
            @Param("genreId") Long genreId,
            @Param("limit") int limit
    );


}
