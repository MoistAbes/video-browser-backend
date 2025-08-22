package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.models.entities.user.UserIconEntity;
import dev.zymion.video.browser.app.repositories.user.UserIconRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserIconService {

    private final UserIconRepository userIconRepository;

    public UserIconService(UserIconRepository userIconRepository) {
        this.userIconRepository = userIconRepository;
    }

    public List<UserIconEntity> getUserIcons() {
        return userIconRepository.findAll();
    }


}
