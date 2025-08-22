package dev.zymion.video.browser.app.repositories.user;

import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, Long> {

    @Query("SELECT ui FROM UserInfoEntity ui WHERE ui.username = :username")
    Optional<UserInfoEntity> findByUsername(@Param("username") String username);

    @Query("SELECT ui FROM UserInfoEntity ui WHERE ui.id != :userId")
    List<UserInfoEntity> findAllFriends(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserInfoEntity u SET u.iconColor = :iconColor WHERE u.id = :userId")
    void updateIconColor(@Param("userId") Long userId, @Param("iconColor") String iconColor);

}
