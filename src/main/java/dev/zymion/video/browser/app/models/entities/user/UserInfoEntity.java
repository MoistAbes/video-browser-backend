package dev.zymion.video.browser.app.models.entities.user;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_info")
public class UserInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true ,nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;

    private String iconColor = "#f1c27d";// np. "#f1c27d"

    @JoinColumn(name = "icon_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private UserIconEntity icon;

    @JoinColumn(name = "status_id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserStatusEntity status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(nullable = false)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private boolean active = true; // domyślnie użytkownik jest aktywny


    @Override
    public String toString() {
        return "UserInfoEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }
}

