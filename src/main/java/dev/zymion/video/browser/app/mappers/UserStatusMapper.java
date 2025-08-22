package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.user.UserStatusDto;
import dev.zymion.video.browser.app.models.entities.user.UserStatusEntity;
import org.springframework.stereotype.Service;

@Service
public class UserStatusMapper {


    public UserStatusDto mapToDto(UserStatusEntity userStatus) {
        return new UserStatusDto(
                userStatus.getId(),
                userStatus.getVideoTitle().orElse(null),
                userStatus.isOnline(),
                userStatus.isWatching()
        );
    }


}
