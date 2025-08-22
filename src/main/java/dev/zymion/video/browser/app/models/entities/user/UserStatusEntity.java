package dev.zymion.video.browser.app.models.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Optional;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoTitle;
    private boolean isOnline = false;
    private boolean isWatching = false;

    public Optional<String> getVideoTitle() {
        return Optional.of(videoTitle);
    }
}
