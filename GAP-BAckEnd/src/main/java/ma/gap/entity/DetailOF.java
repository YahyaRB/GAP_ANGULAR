package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailOF extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    private String sousOfCode;
    private float quantite;
    private float quantiteLivre;
    private String description;

    @ManyToOne
    @JoinColumn(name = "idOF", nullable = false)
    private OrdreFabrication ordreFabrication;

    @Column(columnDefinition = "integer default 0")
    private int compteur;
}
