package dev.zymion.video.browser.app.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "video_details")
public class VideoDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    // Dla seriali/anime
    private String parentTitle; // np. "Breaking Bad", "Naruto"
    private Integer season;     // np. 1
    private Integer episode;    // np. 5

    @JsonBackReference
    @OneToOne(mappedBy = "videoDetails")
    private VideoInfoEntity videoInfo;


    @Override
    public String toString() {
        return "VideoDetailsEntity{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", parentTitle='" + parentTitle + '\'' +
                ", season=" + season +
                ", episode=" + episode +
                '}';
    }
}
