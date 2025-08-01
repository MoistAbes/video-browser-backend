package dev.zymion.video.browser.app.repositories;

import dev.zymion.video.browser.app.entities.VideoInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoInfoRepository extends JpaRepository<VideoInfoEntity, Long> {

    @Query("SELECT v FROM VideoInfoEntity v")
    List<VideoInfoEntity> findAllWithoutDetails();

    @Query("""
    SELECT v
    FROM VideoInfoEntity v
    WHERE v.id IN (
        SELECT MIN(v2.id)
        FROM VideoInfoEntity v2
        GROUP BY v2.videoDetails.parentTitle
    )
""")
    List<VideoInfoEntity> findOnePerParentTitle();


    @Query("SELECT v FROM VideoInfoEntity v WHERE v.videoDetails.parentTitle = :parentTitle ORDER BY v.videoDetails.season, v.videoDetails.episode")
    List<VideoInfoEntity> findAllByParentTitle(@Param("parentTitle") String parentTitle);


    @Query("SELECT v FROM VideoInfoEntity v WHERE v.videoTechnicalDetails.videoHash = :videoHash")
    Optional<VideoInfoEntity> findByVideoHash(@Param("videoHash") String videoHash);

    @Query("SELECT v FROM VideoInfoEntity v WHERE v.rootPath = :rootPath AND v.videoDetails.fileName = :fileName")
    Optional<VideoInfoEntity> findByRootPathAndVideoDetails_FileName(@Param("rootPath") String rootPath, @Param("fileName") String fileName);
}
