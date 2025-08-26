package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.enums.RoleEnum;
import dev.zymion.video.browser.app.exceptions.RoleNotFoundException;
import dev.zymion.video.browser.app.exceptions.UserAlreadyExistsException;
import dev.zymion.video.browser.app.exceptions.UserNotFoundException;
import dev.zymion.video.browser.app.mappers.UserInfoMapper;
import dev.zymion.video.browser.app.models.dto.AuthRequestDto;
import dev.zymion.video.browser.app.models.dto.user.UserInfoDto;
import dev.zymion.video.browser.app.models.dto.user.UserInfoWithStatusDto;
import dev.zymion.video.browser.app.models.entities.user.RoleEntity;
import dev.zymion.video.browser.app.models.entities.user.UserIconEntity;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import dev.zymion.video.browser.app.models.entities.user.UserStatusEntity;
import dev.zymion.video.browser.app.repositories.user.RoleRepository;
import dev.zymion.video.browser.app.repositories.user.UserIconRepository;
import dev.zymion.video.browser.app.repositories.user.UserInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserInfoMapper userInfoMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserIconRepository userIconRepository;

    public UserInfoService(UserInfoRepository userInfoRepository, UserInfoMapper userInfoMapper, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserIconRepository userIconRepository) {
        this.userInfoRepository = userInfoRepository;
        this.userInfoMapper = userInfoMapper;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userIconRepository = userIconRepository;
    }

    public void createUser(AuthRequestDto authRequestDto) {

        RoleEntity userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new RoleNotFoundException(RoleEnum.USER));

        UserIconEntity defaultIcon = userIconRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No icons found"));

        UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                .username(authRequestDto.username())
                .password(passwordEncoder.encode(authRequestDto.password()))
                .status(UserStatusEntity.builder().build())
                .iconColor("#f1c27d")
                .icon(defaultIcon)
                .roles(Set.of(userRole))
                .build();

        userInfoRepository.save(userInfoEntity);
    }

    public void registerUser(AuthRequestDto request) {
        if (userInfoRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException(request.username());
        }

        createUser(request);
    }

    public List<UserInfoWithStatusDto> findAllFriends(Long userId) {
        List<UserInfoEntity> users = userInfoRepository.findAllFriends(userId);
        return userInfoMapper.mapToDtoListWithStatus(users);
    }



    public UserInfoDto findById(Long userId) {
        return userInfoRepository.findById(userId)
                .map(userInfoMapper::mapToDto)
                .orElseThrow(UserNotFoundException::new); // Supplier
    }


    @Transactional
    public void updateUserIconAndColor(Long userId, Long iconId, String iconColor) {
        UserInfoEntity user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

        UserIconEntity icon = userIconRepository.findById(iconId)
                .orElseThrow(() -> new RuntimeException("Icon not found"));

        user.setIcon(icon);
        user.setIconColor(iconColor);

        userInfoRepository.save(user);
    }

    public void updateUserOnlineStatus(Long userId, boolean isOnline) {
        userInfoRepository.updateOnline(userId, isOnline);
    }
}

