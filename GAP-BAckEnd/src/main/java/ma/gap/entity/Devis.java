package ma.gap.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Devis extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String numDevis;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateDevis;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;
}
