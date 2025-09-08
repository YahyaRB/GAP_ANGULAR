package ma.gap.dtos;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueDTO {
    private Long id;
    private String evenement;
    private String faitPar;
    private String validePar;
    private String TypeHistorique;
    private Date date;

}
