package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.models.entities.show.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {

    @Query("SELECT g.name FROM GenreEntity g")
    List<String> findAllGenreNames();
}
