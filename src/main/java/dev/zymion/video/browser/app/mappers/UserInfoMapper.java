package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.UserInfoDto;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoMapper {


    public UserInfoDto mapToDto(UserInfoEntity userInfo) {
        return new UserInfoDto(
                userInfo.getId(),
                userInfo.getUsername()
        );
    }

    public List<UserInfoDto> mapToDtoList(List<UserInfoEntity> userInfoEntities) {
        return userInfoEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

}
