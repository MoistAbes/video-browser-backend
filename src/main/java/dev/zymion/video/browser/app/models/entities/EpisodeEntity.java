//package dev.zymion.video.browser.app.entities;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//@Table(name = "episodes")
//public class EpisodeEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne()
//    @JoinColumn(name = "season_id", nullable = false)
//    private SeasonEntity season;
//
//    @OneToOne
//    @JoinColumn(name = "media_item_id", nullable = false)
//    private MediaItemEntity mediaItem;
//
//
//    @Override
//    public String toString() {
//        return "EpisodeEntity{" +
//                "videoInfo=" + mediaItem.getTitle() +
//                '}';
//    }
//}
