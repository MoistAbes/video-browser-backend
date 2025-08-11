package dev.zymion.video.browser.app.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "episodes")
public class EpisodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;

    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    @OneToOne
    @JoinColumn(name = "video_info_id", nullable = false)
    private VideoInfoEntity videoInfo;


    @Override
    public String toString() {
        return "EpisodeEntity{" +
                "videoInfo=" + videoInfo.getTitle() +
                '}';
    }
}
