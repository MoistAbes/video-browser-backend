package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.models.entities.show.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<ContentEntity, Long> {

    @Query("SELECT c FROM ContentEntity c WHERE c.mediaItem.id = :mediaItemId")
    Optional<ContentEntity> findByMediaItemId(@Param("mediaItemId") Long mediaItemId);

}
