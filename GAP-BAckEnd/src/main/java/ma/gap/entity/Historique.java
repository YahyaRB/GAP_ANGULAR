package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.gap.enums.TypeHistorique;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Historique extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String indice;

    private String description;
    @Enumerated(EnumType.STRING)
    private TypeHistorique type;

    private Date date;


    @ManyToOne
    @JoinColumn(name = "fait_par_id")
    private User faitPar;

    @ManyToOne
    @JoinColumn(name = "valide_par_id")
    private User validePar;

    @ManyToOne
    @JoinColumn(name = "numero_plan_id")
    private Plan numeroPlan;

}
