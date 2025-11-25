package dev.zymion.video.browser.app.controllers.security;

import dev.zymion.video.browser.app.exceptions.UserDisabledException;
import dev.zymion.video.browser.app.models.dto.AuthRequestDto;
import dev.zymion.video.browser.app.models.dto.JwtTokenDto;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import dev.zymion.video.browser.app.repositories.user.UserInfoRepository;
import dev.zymion.video.browser.app.services.UserInfoService;
import dev.zymion.video.browser.app.services.security.JwtService;
import dev.zymion.video.browser.app.services.security.LoginAttemptService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserInfoRepository userInfoRepository;
    private final UserInfoService userInfoService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserInfoRepository userInfoRepository,
                          UserInfoService userInfoService, LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userInfoRepository = userInfoRepository;
        this.userInfoService = userInfoService;
        this.loginAttemptService = loginAttemptService;
    }


    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@Valid @RequestBody AuthRequestDto request) {

        if (loginAttemptService.isBlocked(request.username())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(null); // lub własny DTO z informacją o blokadzie
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            loginAttemptService.loginSucceeded(request.username()); // reset licznika przy sukcesie

            UserInfoEntity user = userInfoRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isActive()) {
                throw new UserDisabledException("This account is disabled");
            }

            String jwtToken = jwtService.generateToken(user);
            return ResponseEntity.ok(new JwtTokenDto(jwtToken));

        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(request.username()); // zwiększamy licznik przy błędzie
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

//    @PostMapping("/login")
//    public ResponseEntity<JwtTokenDto> login(@Valid @RequestBody AuthRequestDto request) {
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.username(), request.password())
//        );
//        log.info("is authenticated");
//
//        UserInfoEntity user = userInfoRepository.findByUsername(request.username())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        if (!user.isActive()) {
//            throw new UserDisabledException("This account is disabled");
//        }
//
//        String jwtToken = jwtService.generateToken(user);
//        return ResponseEntity.ok(new JwtTokenDto(jwtToken));
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody AuthRequestDto request) {
        userInfoService.registerUser(request);
        return ResponseEntity.ok().build();
    }

}
