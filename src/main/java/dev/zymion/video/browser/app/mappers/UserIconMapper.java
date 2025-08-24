package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.user.UserIconDto;
import dev.zymion.video.browser.app.models.entities.user.UserIconEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserIconMapper {

    public UserIconDto mapToDto(UserIconEntity userIcon) {
        return new UserIconDto(
                userIcon.getId(),
                userIcon.getName()
        );
    }

    public List<UserIconDto> mapToDtoList(List<UserIconEntity> userIcons) {
        return userIcons.stream()
                .map(this::mapToDto)
                .toList();
    }

}
