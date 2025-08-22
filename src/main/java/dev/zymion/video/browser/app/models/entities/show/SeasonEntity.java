package dev.zymion.video.browser.app.models.entities.show;

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
@Table(name = "seasons")
public class SeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "season_id") // kolumna w ContentEntity
    private List<ContentEntity> episodes = new ArrayList<>();

    @Override
    public String toString() {
        return "SeasonEntity{" +
                "id=" + id +
                "number=" + number +
                "episodes=" + episodes +
                '}';
    }
}
