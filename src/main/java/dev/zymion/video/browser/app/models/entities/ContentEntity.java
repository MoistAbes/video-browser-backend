package dev.zymion.video.browser.app.models.entities;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaTypeEnum type; // MOVIE or EPISODE

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private ShowEntity show;

    @ManyToOne
    @JoinColumn(name = "season_id") // nullable — tylko dla EPISODE
    private SeasonEntity season;

    @OneToOne
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItemEntity mediaItem;

}
