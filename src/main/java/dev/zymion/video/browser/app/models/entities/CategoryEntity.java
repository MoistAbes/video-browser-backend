package dev.zymion.video.browser.app.models.entities;

import dev.zymion.video.browser.app.enums.CategoryEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // <--- to jest kluczowe
    @Column(nullable = false, unique = true)
    private CategoryEnum name;
}
