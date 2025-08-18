package dev.zymion.video.browser.app.models.entities;

import dev.zymion.video.browser.app.enums.MediaTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "media_item")
public class MediaItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String parentTitle;
    private Integer seasonNumber;
    private Integer episodeNumber;
    @Enumerated(EnumType.STRING)
    private MediaTypeEnum type; // MOVIE / EPISODE
    @Column(nullable = false)
    private String rootPath;
    @Column(nullable = false)
    private String fileName;

    private String codec;
    private String audio;
    private double duration;

    @Column(nullable = false)
    private String videoHash;



    public Optional<Integer> getSeasonNumber() {
        return Optional.ofNullable(seasonNumber);
    }

    public Optional<Integer> getEpisodeNumber() {
        return Optional.ofNullable(episodeNumber);
    }

    public Optional<String> getCodec() {
        return Optional.ofNullable(codec);
    }

    public Optional<String> getAudio() {
        return Optional.ofNullable(audio);
    }
}