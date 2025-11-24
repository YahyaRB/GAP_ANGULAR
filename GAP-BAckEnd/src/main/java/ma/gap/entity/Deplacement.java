package ma.gap.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Deplacement extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private Integer nmbJours;
    private String motif;
    private String pieceJointe;
    @Column(name = "flag", nullable = false)
    private int flag=1;
    @ManyToMany
    @JoinTable(name = "Deplacement_employee",
            joinColumns = @JoinColumn(name = "id_deplacement"),
            inverseJoinColumns = @JoinColumn(name = "id_employee"))
    private List<Employee> employee;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_motif")
    private MotifDeplacement motifDeplacement;

    @ManyToOne(targetEntity = Projet.class)
    @JoinColumn(name = "projet",referencedColumnName = "code")
    private Projet projet;
}
