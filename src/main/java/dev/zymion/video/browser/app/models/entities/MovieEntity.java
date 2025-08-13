//package dev.zymion.video.browser.app.entities;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//@Table(name = "movies")
//public class MovieEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "show_id", nullable = false)
//    private ShowEntity show;
//
//    @OneToOne
//    @JoinColumn(name = "media_item_id", nullable = false)
//    private MediaItemEntity mediaItem;
//
//    @Override
//    public String toString() {
//        return "MovieEntity{" +
//                "videoInfo=" + mediaItem.getTitle() +
//                '}';
//    }
//}
