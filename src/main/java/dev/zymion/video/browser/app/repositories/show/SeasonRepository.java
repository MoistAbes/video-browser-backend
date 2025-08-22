package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.models.entities.show.ShowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepository extends JpaRepository<ShowEntity, Long> {
}
