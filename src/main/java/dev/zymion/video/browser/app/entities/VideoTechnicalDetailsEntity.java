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
@Table(name = "video_technical_details")
public class VideoTechnicalDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codec;
    private String audio;
    private String videoHash;

    @JsonBackReference
    @OneToOne(mappedBy = "videoTechnicalDetails")
    private VideoInfoEntity videoInfo;


    @Override
    public String toString() {
        return "VideoTechnicalDetailsEntity{" +
                "id=" + id +
                ", codec='" + codec + '\'' +
                ", audio='" + audio + '\'' +
                '}';
    }
}
