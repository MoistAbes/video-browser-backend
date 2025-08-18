package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.enums.RoleEnum;
import dev.zymion.video.browser.app.exceptions.RoleNotFoundException;
import dev.zymion.video.browser.app.exceptions.UserAlreadyExistsException;
import dev.zymion.video.browser.app.models.dto.JwtTokenDto;
import dev.zymion.video.browser.app.models.dto.AuthRequestDto;
import dev.zymion.video.browser.app.models.entities.user.RoleEntity;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import dev.zymion.video.browser.app.repositories.RoleRepository;
import dev.zymion.video.browser.app.repositories.UserInfoRepository;
import dev.zymion.video.browser.app.services.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserInfoService(UserInfoRepository userInfoRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userInfoRepository = userInfoRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public UserInfoEntity createUser(AuthRequestDto authRequestDto) {

        RoleEntity userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new RoleNotFoundException(RoleEnum.USER));

        UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                .username(authRequestDto.username())
                .password(passwordEncoder.encode(authRequestDto.password()))
                .roles(Set.of(userRole))
                .build();

        return userInfoRepository.save(userInfoEntity);
    }


    public JwtTokenDto registerUser(AuthRequestDto request) {
        if (userInfoRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException(request.username());
        }

        UserInfoEntity user = createUser(request);
        String token = jwtService.generateToken(user);

        return new JwtTokenDto(token);
    }

}

