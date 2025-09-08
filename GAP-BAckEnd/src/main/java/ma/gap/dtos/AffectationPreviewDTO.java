package ma.gap.dtos;

import lombok.Data;
import java.util.Date;

@Data
public class AffectationPreviewDTO {
    private String tempId; // ID temporaire pour l'interface
    private Long employeeId;
    private String employeeName;
    private String employeeMatricule;
    private Long atelierId;
    private String atelierDesignation;
    private Long projetId;
    private String projetCode;
    private String projetDesignation;
    private Long articleId;
    private String articleNumPrix;
    private String articleDesignation;
    private Date date;
    private String periode;
    private Integer nombreHeures;
    private boolean canModifyHours; // true pour "Heures" et "Heures_Sup"
    private boolean hasConflict;
    private String conflictMessage;
}
