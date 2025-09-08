package ma.gap.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(	name = "login",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User extends Auditable<String> implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(max = 20)
    private String username;
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    @NotBlank
    @Size(max = 120)
    private String password;
    @NotBlank
    @Size(max = 20)
    private String nom;
    @NotBlank
    @Size(max = 20)
    private String prenom;
    @NotBlank
    @Size(max = 10)
    private String session;

    @Size(max = 10)
    private String matricule;

    private String resetToken;
    private LocalDateTime dateToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "login_role",
            joinColumns = @JoinColumn(name = "id_login"),
            inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private List<Role> roles;

    @ManyToMany
    @JoinTable(name = "user_atelier",
            joinColumns = @JoinColumn(name = "id_login"),
            inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private List<Ateliers> ateliers;
    /*@ManyToMany()
    @JoinTable(	name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles ;
    @ManyToMany()
    @JoinTable(	name = "user_atelier",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "atelier_id"))
    private List<Ateliers> ateliers ;*/


}
