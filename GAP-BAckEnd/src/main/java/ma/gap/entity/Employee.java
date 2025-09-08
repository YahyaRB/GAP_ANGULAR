package ma.gap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.catalina.valves.rewrite.InternalRewriteMap;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "matricule")})
public class Employee  extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nom;
    private String prenom;
    @Column(unique = true)
    private String matricule;

    @ManyToOne
    @JoinColumn(name = "id_ateliers")
    private Ateliers ateliers;
    
    @ManyToOne(targetEntity = Fonction.class)
    @JoinColumn(name = "code_fonction", referencedColumnName = "codeFonction")
    private Fonction fonction;



}
