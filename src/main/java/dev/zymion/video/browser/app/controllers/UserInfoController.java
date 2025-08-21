package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.dto.UserInfoDto;
import dev.zymion.video.browser.app.services.UserInfoService;
import dev.zymion.video.browser.app.services.security.SecurityUtilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserInfoController {

    private final UserInfoService userInfoService;
    private final SecurityUtilService securityUtilService;


    public UserInfoController(UserInfoService userInfoService, SecurityUtilService securityUtilService) {
        this.userInfoService = userInfoService;
        this.securityUtilService = securityUtilService;
    }


    @GetMapping("/friends")
    public ResponseEntity<List<UserInfoDto>> findAllFriends() {
//        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Long userId = userDetails.getId();
        Long userId = securityUtilService.getCurrentUserId();

        List<UserInfoDto> friends = userInfoService.findAllFriends(userId);

        return ResponseEntity.ok(friends);
    }

}
