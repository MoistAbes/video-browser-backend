package dev.zymion.video.browser.app.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "seasons")
public class SeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private ShowEntity show;

    @JsonManagedReference
    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number ASC")
    private Set<EpisodeEntity> episodes = new HashSet<>();

    @Override
    public String toString() {
        return "SeasonEntity{" +
                "episodes=" + episodes +
                '}';
    }
}
