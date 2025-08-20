package dev.zymion.video.browser.app.models.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "show_id") // kolumna w SeasonEntity
    private List<SeasonEntity> seasons = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "show_id") // kolumna w ContentEntity
    private List<ContentEntity> movies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_structure_id")
    private ShowStructureEntity structure;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "shows_genres", // nazwa tabeli pośredniczącej
            joinColumns = @JoinColumn(name = "show_id"), // kolumna wskazująca kategorię
            inverseJoinColumns = @JoinColumn(name = "genre_id") // kolumna wskazująca gatunek
    )
    private Set<GenreEntity> genres = new HashSet<>();

    @Override
    public String toString() {
        return "ShowEntity{" +
                "name='" + name + '\'' +
                ", seasons=" + seasons +
                ", movies=" + movies +
                '}';
    }
}
