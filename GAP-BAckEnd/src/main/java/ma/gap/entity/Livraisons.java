package ma.gap.entity;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Livraisons extends Auditable<String> implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false,unique = false)
	private long id;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dateLivraison;

	@ManyToOne()
	@JoinColumn(name = "idChauffeur")
	private Chauffeur chauffeur;

	@ManyToOne()
	@JoinColumn(name = "idProjet")
	private Projet projet;


	@ManyToOne()
	@JoinColumn(name = "idAtelier")
	private Ateliers atelier;


	public void setChauffeur(Chauffeur chauffeur) {
		this.chauffeur = chauffeur;
	}

}
