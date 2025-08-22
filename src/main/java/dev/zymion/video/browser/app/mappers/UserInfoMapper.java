package dev.zymion.video.browser.app.mappers;

import dev.zymion.video.browser.app.models.dto.user.UserInfoDto;
import dev.zymion.video.browser.app.models.dto.user.UserInfoWithStatusDto;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserInfoMapper {

    private final UserStatusMapper userStatusMapper;

    public UserInfoMapper(UserStatusMapper userStatusMapper) {
        this.userStatusMapper = userStatusMapper;
    }

    public UserInfoDto mapToDto(UserInfoEntity userInfo) {
        return new UserInfoDto(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getIconColor(),
                userInfo.getIcon().getName()
        );
    }

    public List<UserInfoDto> mapToDtoList(List<UserInfoEntity> userInfoEntities) {
        return userInfoEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserInfoWithStatusDto mapToDtoWithStatus(UserInfoEntity userInfo) {

        return new UserInfoWithStatusDto(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getIconColor(),
                userInfo.getIcon().getName(),
                userStatusMapper.mapToDto(userInfo.getStatus())
        );

    }

    public List<UserInfoWithStatusDto> mapToDtoListWithStatus(List<UserInfoEntity> userInfoEntities) {
        return userInfoEntities.stream()
                .map(this::mapToDtoWithStatus)
                .toList();
    }
}
