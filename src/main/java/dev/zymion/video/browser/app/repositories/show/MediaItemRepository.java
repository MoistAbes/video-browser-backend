package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.models.entities.show.MediaItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaItemRepository extends JpaRepository<MediaItemEntity, Long> {

    @Query("SELECT m FROM MediaItemEntity m WHERE m.videoHash = :videoHash")
    Optional<MediaItemEntity> findByVideoHash(@Param("videoHash") String videoHash);

    @Query("SELECT m FROM MediaItemEntity m WHERE m.audio IN :unsupported")
    List<MediaItemEntity> findAllWithUnsupportedAudioCodecs(@Param("unsupported") List<String> unsupported);

}
