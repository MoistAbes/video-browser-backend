package dev.zymion.video.browser.app.models.entities;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShowStructureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private StructureTypeEnum name;


}
