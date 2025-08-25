package dev.zymion.video.browser.app.models.entities.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_status")
public class UserStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoTitle;
    private boolean isOnline = false;
    private boolean isWatching = false;

    public Optional<String> getVideoTitle() {
        return Optional.ofNullable(videoTitle);
    }
}
