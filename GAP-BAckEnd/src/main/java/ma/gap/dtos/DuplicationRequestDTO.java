package ma.gap.dtos;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class DuplicationRequestDTO {
    private Long atelierId;
    private Date sourceDate;
    private Date targetDate;
    private List<String> periodes;
}