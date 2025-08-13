package dev.zymion.video.browser.app.models.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "shows",
        indexes = {
                @Index(name = "idx_show_name", columnList = "name", unique = true)
        }
)
public class ShowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String rootPath;

    @OneToMany(fetch = FetchType.LAZY ,mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeasonEntity> seasons = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentEntity> movies = new ArrayList<>();

    @Override
    public String toString() {
        return "ShowEntity{" +
                "name='" + name + '\'' +
                ", seasons=" + seasons +
                ", movies=" + movies +
                '}';
    }
}
