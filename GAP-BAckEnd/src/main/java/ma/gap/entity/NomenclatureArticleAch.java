package ma.gap.entity;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
 @Setter

 public class NomenclatureArticleAch implements Serializable {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    private Nomenclature nomenclature;

	    @ManyToOne
	    private ArticleAch articleAch;

	    private Float largeur;
	    private Float longueur;
	    private Float epaisseur;
	    private String finition;
	    private Float quantite;
 }

