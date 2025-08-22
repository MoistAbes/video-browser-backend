package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.entities.user.UserIconEntity;
import dev.zymion.video.browser.app.services.UserIconService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/user-icon")
public class UserIconController {

    private final UserIconService userIconService;

    public UserIconController(UserIconService userIconService) {
        this.userIconService = userIconService;
    }

    @GetMapping("/find-all")
    public ResponseEntity<List<UserIconEntity>> findAllUserIcons() {
        List<UserIconEntity> icons = userIconService.getUserIcons();

        return ResponseEntity.ok(icons);
    }

}
