package ma.gap.dtos;

import lombok.Data;

import java.util.List;

@Data
public class DeliveryRequest {
    private Long ofId;
    private List<Long> sousOfIds; // Liste des sous-OF à livrer
}