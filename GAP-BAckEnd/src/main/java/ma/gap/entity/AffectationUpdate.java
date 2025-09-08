package ma.gap.entity;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity

@Data

@NoArgsConstructor
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class AffectationUpdate extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String periode;
    private Integer nombreHeures;


    @ManyToOne
    @JoinColumn(name = "employe_id")
    private Employee employees;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projets;

    @ManyToOne
    @JoinColumn(name = "Atelier_id")
    private Ateliers ateliers;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

}
