package dev.zymion.video.browser.app.repositories.show;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.models.entities.show.ShowStructureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShowStructureRepository extends JpaRepository<ShowStructureEntity, Long> {


    @Query("SELECT s FROM ShowStructureEntity s WHERE s.name = :name")
    Optional<ShowStructureEntity> findByName(@Param("name") StructureTypeEnum name);

    @Query("SELECT s.id FROM ShowStructureEntity s WHERE s.name = :name")
    Long findIdByName(@Param("name") StructureTypeEnum name);
}
