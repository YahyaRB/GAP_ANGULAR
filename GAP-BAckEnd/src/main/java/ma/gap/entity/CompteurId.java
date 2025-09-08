package ma.gap.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CompteurId implements Serializable{
	
	private int annee;
    private int atelier;

}
