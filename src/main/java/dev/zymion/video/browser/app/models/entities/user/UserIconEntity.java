package dev.zymion.video.browser.app.models.entities.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_icon")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserIconEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; //fa-solid fa-face-flushed
}
