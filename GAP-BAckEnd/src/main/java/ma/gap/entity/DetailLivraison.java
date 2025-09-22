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
    @JoinColumn(name="idOF", nullable = true)
    private OrdreFabrication ordreFabrication;

    @ManyToOne
    @JoinColumn(name="id_nomenclature", nullable = true)
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

    public void setLivraison(Livraisons livraisons) {  // CORRECTION : nom de la méthode
        this.livraison = livraisons;
    }

    public void setImprime(boolean imprime) {
        this.imprime = imprime;
    }

    public boolean getImprime() {
        return this.imprime;
    }
}