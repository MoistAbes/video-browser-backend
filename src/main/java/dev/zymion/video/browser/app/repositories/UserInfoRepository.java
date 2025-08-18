package dev.zymion.video.browser.app.repositories;

import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, Long> {

    @Query("SELECT ui FROM UserInfoEntity ui WHERE ui.username = :username")
    Optional<UserInfoEntity> findByUsername(@Param("username") String username);

}
