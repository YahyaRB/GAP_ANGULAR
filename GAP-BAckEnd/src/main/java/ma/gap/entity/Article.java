package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Article extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String numPrix;
    private String designation;
    private float quantiteTot;
    private float quantiteProd;
    private float quantiteEnProd;
    private float quantiteLivre;
    private float quantitePose;;
    private String unite;
    private String texteLibre;
    @Enumerated(EnumType.STRING)
    private OrigineArticle origineArticle;
    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @ManyToOne
    @JoinColumn(name = "ateliers_id")
    private Ateliers ateliers;
    @ManyToOne
    @JoinColumn(name = "devis_id", nullable = true)
    private Devis devis;

    public enum OrigineArticle {
        MARCHE,
        DEVIS,
        AUTRE
    }
}
