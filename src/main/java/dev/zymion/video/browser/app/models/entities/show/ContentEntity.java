package dev.zymion.video.browser.app.models.entities.show;

import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "contents")
public class ContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //ToDO to samo pole mamy w mediaItem wiec musi zostac albo tu albo w media item duplikacja jest nipotrzebna
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaTypeEnum type; // MOVIE or EPISODE

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItemEntity mediaItem;

}
