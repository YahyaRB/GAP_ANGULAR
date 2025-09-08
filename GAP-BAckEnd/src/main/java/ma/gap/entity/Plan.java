package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.gap.enums.StatutPlan;
import ma.gap.exceptions.PlanNotFoundException;

import java.util.List;
import javax.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "plans")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Plan extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatutPlan statut ;

    @Column(name = "niveau")
    private String niveau;

    @Column(name = "emplacement")
    private String emplacement;

    @Column(name = "datePlan")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date datePlan;

    
    @Column(name = "dateFin")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFin;
    
    @Column(name = "pieceJointe")
    private String pieceJointe;

    @ManyToOne
    @JoinColumn(name = "affaire_id")
    private Projet affaire;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne
    @JoinColumn(name = "atelier_id")
    private Ateliers atelier;
    
    @ManyToOne
    @JoinColumn(name = "dessine_par_id")
    private User dessinePar;

    @ManyToOne
    @JoinColumn(name = "control_par_id")
    private User controlPar;


}









