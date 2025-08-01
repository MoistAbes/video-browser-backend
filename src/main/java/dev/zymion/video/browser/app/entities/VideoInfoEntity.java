package dev.zymion.video.browser.app.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.zymion.video.browser.app.enums.VideoTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "video_info")
public class VideoInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String iconFileName;
    private String rootPath;
    private VideoTypeEnum type;
    private String category;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "video_details_id")
    VideoDetailsEntity videoDetails;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "video_technical_details_id")
    VideoTechnicalDetailsEntity videoTechnicalDetails;


    @Override
    public String toString() {
        return "VideoInfoEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", iconFilePath='" + iconFileName + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", videoDetails=" + videoDetails +
                ", videoTechnicalDetails=" + videoTechnicalDetails +
                '}';
    }
}
