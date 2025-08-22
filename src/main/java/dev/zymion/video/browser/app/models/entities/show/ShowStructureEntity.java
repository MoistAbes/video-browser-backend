package dev.zymion.video.browser.app.models.entities.show;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "show_structures")
public class ShowStructureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // <--- to jest kluczowe
    @Column(nullable = false, unique = true)
    private StructureTypeEnum name;


}
