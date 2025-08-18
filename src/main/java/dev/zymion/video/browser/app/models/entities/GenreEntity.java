package dev.zymion.video.browser.app.models.entities;

import dev.zymion.video.browser.app.enums.GenreEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "genres")
public class GenreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // <--- to jest kluczowe
    @Column(nullable = false, unique = true)
    private GenreEnum name;

}
