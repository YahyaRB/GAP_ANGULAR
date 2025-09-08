package ma.gap.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.gap.enums.StatutEntity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdreFabrication extends Auditable<String> implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false,unique = false)
	private Long id;

	private String numOF;
	@Column(columnDefinition = "float default 0.0")
	private double qteRest;
	@Column(columnDefinition = "float default 0.0")
	private double qteLivre;
	@ManyToOne
    @JoinColumn(name = "atelier_id")
	private Ateliers atelier;
	
	@ManyToOne
    @JoinColumn(name = "projet_id")
	private Projet projet;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date date;
	
	@ManyToOne
    @JoinColumn(name = "article_id")
	private Article article;
	
	@ManyToOne
    @JoinColumn(name = "plan_id")
	private Plan plan;
	
	private double quantite;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dateFin;
	
	private double tempsPrevu;
	
	@Column(length=690)
	private String description;
	
	private StatutEntity statut;

	@Column(columnDefinition = "integer default 0")
	private int compteur;
	
	@Column(columnDefinition = "integer default 0")
	private int avancement;
	
	private String pieceJointe;
	
	@Column(columnDefinition = "integer default 1")
	private int flag;
	
	//@Column
	//private double quantiteLivre;
	

}
