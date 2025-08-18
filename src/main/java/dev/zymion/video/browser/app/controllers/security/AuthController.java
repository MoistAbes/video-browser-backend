package dev.zymion.video.browser.app.controllers.security;

import dev.zymion.video.browser.app.models.dto.AuthRequestDto;
import dev.zymion.video.browser.app.models.dto.JwtTokenDto;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import dev.zymion.video.browser.app.repositories.UserInfoRepository;
import dev.zymion.video.browser.app.services.UserInfoService;
import dev.zymion.video.browser.app.services.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserInfoRepository userInfoRepository,
                          UserInfoService userInfoService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userInfoRepository = userInfoRepository;
        this.userInfoService = userInfoService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody AuthRequestDto request) {
        //ToDO tego moge uzyc do wyciagania danych z przeslanego tokena
        //CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //Long userId = userDetails.getId();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        log.info("is authenticated");

        UserInfoEntity user = userInfoRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new JwtTokenDto(jwtToken));
    }


    @PostMapping("/register")
    public ResponseEntity<JwtTokenDto> register(@RequestBody AuthRequestDto request) {
        JwtTokenDto token = userInfoService.registerUser(request);
        return ResponseEntity.ok(token);
    }

}
