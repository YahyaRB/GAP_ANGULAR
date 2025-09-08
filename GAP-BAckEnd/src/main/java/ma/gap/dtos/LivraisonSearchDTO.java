package ma.gap.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivraisonSearchDTO {
    private String numero;
    private Date dateDebut;
    private Date dateFin;
    private String affaire;
    private String client;
    private String banque;
    private String type;
    private String statut;
}
