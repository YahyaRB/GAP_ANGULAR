package ma.gap.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class DetailLivraison extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false,unique = false)
    private long id;
    private float quantite;
    private String emplacement;
    private String observation;
    @Column
    private boolean imprime;
    @ManyToOne
    @JoinColumn(name="idOF")
    private OrdreFabrication ordreFabrication;
    @ManyToOne
    @JoinColumn(name = "idLivraison")
    @JsonIgnore
    private Livraisons livraison;
    
    @ManyToOne
    @JoinColumn(name = "idArticle")
    private Article article;

    public OrdreFabrication getOrdreFabrication() {

        return ordreFabrication;
    }

    public void setOrdreFabrication(OrdreFabrication ordreFabrication) {
        this.ordreFabrication = ordreFabrication;
    }

    public Livraisons getLivraison() {
        return livraison;
    }

    public void setLivraion(Livraisons livraisons) {
        this.livraison = livraisons;
    }

    public void setImprime(boolean imprime) {
        this.imprime = imprime;
    }
    public boolean getImprime() {
        return this.imprime ;
    }
}

