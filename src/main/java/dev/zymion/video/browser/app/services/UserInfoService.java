package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.enums.RoleEnum;
import dev.zymion.video.browser.app.exceptions.RoleNotFoundException;
import dev.zymion.video.browser.app.exceptions.UserAlreadyExistsException;
import dev.zymion.video.browser.app.exceptions.UserNotFoundException;
import dev.zymion.video.browser.app.mappers.UserInfoMapper;
import dev.zymion.video.browser.app.models.dto.AuthRequestDto;
import dev.zymion.video.browser.app.models.dto.UserInfoDto;
import dev.zymion.video.browser.app.models.entities.user.RoleEntity;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import dev.zymion.video.browser.app.repositories.RoleRepository;
import dev.zymion.video.browser.app.repositories.UserInfoRepository;
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

    public UserInfoService(UserInfoRepository userInfoRepository, UserInfoMapper userInfoMapper, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.userInfoMapper = userInfoMapper;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(AuthRequestDto authRequestDto) {

        RoleEntity userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new RoleNotFoundException(RoleEnum.USER));

        UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                .username(authRequestDto.username())
                .password(passwordEncoder.encode(authRequestDto.password()))
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

    public List<UserInfoDto> findAllFriends(Long userId) {
        List<UserInfoEntity> users = userInfoRepository.findAllFriends(userId);
        return userInfoMapper.mapToDtoList(users);
    }

    public void updateIconColor(Long userId ,String iconColor) {
        userInfoRepository.updateIconColor(userId ,iconColor);
    }

    public UserInfoDto findById(Long userId) {
        return userInfoRepository.findById(userId)
                .map(userInfoMapper::mapToDto)
                .orElseThrow(UserNotFoundException::new); // Supplier
    }
}

