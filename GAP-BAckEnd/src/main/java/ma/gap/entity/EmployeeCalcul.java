package ma.gap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCalcul {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private Integer heures;
    private float pourcentage;
}
