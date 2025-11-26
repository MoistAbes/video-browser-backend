package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.dto.user.UserInfoDto;
import dev.zymion.video.browser.app.models.dto.user.UserInfoWithStatusDto;
import dev.zymion.video.browser.app.services.UserInfoService;
import dev.zymion.video.browser.app.services.security.SecurityUtilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/userInfo")
    public ResponseEntity<UserInfoDto> getUserInfo() {
        Long userId = securityUtilService.getCurrentUserId();
        UserInfoDto user = userInfoService.findById(userId);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfoDto>> findAllUsers() {
        List<UserInfoDto> mappedUsers = userInfoService.findAllUsers();

        return ResponseEntity.ok(mappedUsers);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserInfoWithStatusDto>> findAllFriends() {
        Long userId = securityUtilService.getCurrentUserId();

        List<UserInfoWithStatusDto> friends = userInfoService.findAllFriends(userId);

        return ResponseEntity.ok(friends);
    }


    @PutMapping("/update/icon/{iconId}")
    public ResponseEntity<Void> updateIcon(@PathVariable Long iconId, @RequestParam String iconColor) {
        Long userId = securityUtilService.getCurrentUserId();
        userInfoService.updateUserIconAndColor(userId, iconId, iconColor);
        return ResponseEntity.noContent().build();
    }



}
