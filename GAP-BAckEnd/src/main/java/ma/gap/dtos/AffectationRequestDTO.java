package ma.gap.dtos;

import lombok.Data;
import ma.gap.entity.*;
import java.util.Date;
import java.util.List;

@Data
public class AffectationRequestDTO {
    private Date date;
    private String periode;
    private Integer nombreHeures;
    private List<Employee> employees;
    private Projet projets;
    private Ateliers ateliers;
    private Article article;
}