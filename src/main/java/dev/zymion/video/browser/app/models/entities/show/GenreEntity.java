package dev.zymion.video.browser.app.models.entities.show;

import dev.zymion.video.browser.app.enums.GenreEnum;
import dev.zymion.video.browser.app.enums.MediaTypeEnum;
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

    //tutaj bez generation type identity bo chcemy miec te same id co w tmdb api zeby mozna mylo pozniej dobrze matchowac
    @Id
    private Long id;

    @Enumerated(EnumType.STRING) // <--- to jest kluczowe
    @Column(nullable = false)
    private GenreEnum name;


    @Enumerated(EnumType.STRING)
    private MediaTypeEnum mediaType;

}
