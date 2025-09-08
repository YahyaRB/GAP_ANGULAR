package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data

@Table(	name = "chauffeur",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "matricule")
        })

public class Chauffeur extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    @Getter
    @Column(unique = true)
    private Integer matricule;

    public void setMatricule(int matricule) {
        this.matricule = matricule;
    }
}
