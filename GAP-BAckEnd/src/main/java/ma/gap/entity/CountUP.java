package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(CompteurId.class)
public class CountUP extends Auditable<String> implements Serializable {

    @Id
    private int annee;
    @Id
    private int atelier;
    private int countNum;
    
}
