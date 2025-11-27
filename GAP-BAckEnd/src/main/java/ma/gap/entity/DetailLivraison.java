package ma.gap.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ma.gap.enums.TypeDetail;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DetailLivraison extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = false)
    private long id;

    private float quantite;
    private String emplacement;
    private String observation;

    @Column
    private boolean imprime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDetail typeDetail; // OF_COMPLET ou NOMENCLATURE

    @ManyToOne
    @JoinColumn(name = "idOF", nullable = true)
    private OrdreFabrication ordreFabrication;

    @ManyToOne
    @JoinColumn(name = "id_nomenclature", nullable = true)
    private Nomenclature nomenclature;

    @ManyToOne
    @JoinColumn(name = "idLivraison")
    @JsonIgnore
    private Livraisons livraison;

    @ManyToOne
    @JoinColumn(name = "idArticle")
    private Article article;

    // CORRECTION : Implémentation correcte des setters
    public void setTypeDetail(TypeDetail typeDetail) {
        this.typeDetail = typeDetail;
    }

    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    // Autres getters/setters
    public OrdreFabrication getOrdreFabrication() {
        return ordreFabrication;
    }

    public void setOrdreFabrication(OrdreFabrication ordreFabrication) {
        this.ordreFabrication = ordreFabrication;
    }

    public Livraisons getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraisons livraisons) { // CORRECTION : nom de la méthode
        this.livraison = livraisons;
    }

    public void setImprime(boolean imprime) {
        this.imprime = imprime;
    }

    public boolean getImprime() {
        return this.imprime;
    }

    // Méthodes pour l'impression JasperReports
    public String getArticleDesignationImpression() {
        if (this.typeDetail == TypeDetail.OF_COMPLET && this.ordreFabrication != null
                && this.ordreFabrication.getArticle() != null) {
            return this.ordreFabrication.getArticle().getDesignation();
        } else if (this.typeDetail == TypeDetail.NOMENCLATURE && this.nomenclature != null) {
            // Si c'est une nomenclature, on retourne d'abord l'article de l'OF parent si
            // disponible
            if (this.nomenclature.getOrdreFabrication() != null
                    && this.nomenclature.getOrdreFabrication().getArticle() != null) {
                return this.nomenclature.getOrdreFabrication().getArticle().getDesignation();
            }
            // Sinon on retourne la désignation de la nomenclature
            return this.nomenclature.getDesignation();
        }
        return "";
    }

    public String getAffaireCodeImpression() {
        if (this.typeDetail == TypeDetail.OF_COMPLET && this.ordreFabrication != null
                && this.ordreFabrication.getProjet() != null) {
            return this.ordreFabrication.getProjet().getCode();
        } else if (this.typeDetail == TypeDetail.NOMENCLATURE && this.nomenclature != null
                && this.nomenclature.getOrdreFabrication() != null
                && this.nomenclature.getOrdreFabrication().getProjet() != null) {
            return this.nomenclature.getOrdreFabrication().getProjet().getCode();
        }
        return "";
    }

    public String getNumOFImpression() {
        if (this.typeDetail == TypeDetail.OF_COMPLET && this.ordreFabrication != null) {
            return this.ordreFabrication.getNumOF();
        } else if (this.typeDetail == TypeDetail.NOMENCLATURE && this.nomenclature != null
                && this.nomenclature.getOrdreFabrication() != null) {
            return this.nomenclature.getOrdreFabrication().getNumOF();
        }
        return "";
    }

    public java.util.Date getDateOFImpression() {
        if (this.typeDetail == TypeDetail.OF_COMPLET && this.ordreFabrication != null) {
            return this.ordreFabrication.getDate();
        } else if (this.typeDetail == TypeDetail.NOMENCLATURE && this.nomenclature != null
                && this.nomenclature.getOrdreFabrication() != null) {
            return this.nomenclature.getOrdreFabrication().getDate();
        }
        return null;
    }

    public String getCompteurOFImpression() {
        if (this.typeDetail == TypeDetail.OF_COMPLET && this.ordreFabrication != null) {
            return String.valueOf(this.ordreFabrication.getCompteur());
        } else if (this.typeDetail == TypeDetail.NOMENCLATURE && this.nomenclature != null
                && this.nomenclature.getOrdreFabrication() != null) {
            return String.valueOf(this.nomenclature.getOrdreFabrication().getCompteur());
        }
        return "0";
    }
}