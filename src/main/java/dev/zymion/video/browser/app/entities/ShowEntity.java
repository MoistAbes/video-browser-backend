package dev.zymion.video.browser.app.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private Set<SeasonEntity> seasons = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieEntity> movies = new ArrayList<>();


    @Override
    public String toString() {
        return "ShowEntity{" +
                "name='" + name + '\'' +
                ", seasons=" + seasons +
                ", movies=" + movies +
                '}';
    }
}
