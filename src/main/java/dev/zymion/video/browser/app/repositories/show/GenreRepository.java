package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {
}
