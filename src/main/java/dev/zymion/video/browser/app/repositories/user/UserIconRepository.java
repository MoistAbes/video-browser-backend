package dev.zymion.video.browser.app.repositories.user;

import dev.zymion.video.browser.app.models.entities.user.UserIconEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIconRepository extends JpaRepository<UserIconEntity, Long> {
}
