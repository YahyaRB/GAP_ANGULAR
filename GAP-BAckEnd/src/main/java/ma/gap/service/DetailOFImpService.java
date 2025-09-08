package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.dtos.DeliveryRequest;
import ma.gap.entity.DetailOF;
import ma.gap.entity.OrdreFabrication;
import ma.gap.repository.DetailOFRepository;
import ma.gap.repository.OrdreFabricationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DetailOFImpService implements DetailOFService{
    private DetailOFRepository detailOFRepository;
    private OrdreFabricationRepository ordreFabricationRepository;
    @Override
    public DetailOF createDetailOF(Long ordreFabricationId, DetailOF detailOF) {
        // Récupérer l'Ordre de Fabrication associé
        OrdreFabrication of = ordreFabricationRepository.findById(ordreFabricationId)
                .orElseThrow(() -> new RuntimeException("Ordre de Fabrication non trouvé"));

        // Calcul du compteur : obtenir le dernier compteur pour cet OF
        int lastCounter = detailOFRepository.findMaxCompteurByOrdreFabrication(of);
        int newCounter = lastCounter + 1; // Incrémenter le compteur
        detailOF.setCompteur(newCounter);

        // Génération du sousOfCode
        String sousOfCode = "OF" + of.getId() + "-" + newCounter;
        detailOF.setSousOfCode(sousOfCode);

        // Associer l'OF principal
        detailOF.setOrdreFabrication(of);

        // Sauvegarder le sous-OF
        return detailOFRepository.save(detailOF);
    }
   /* public List<DetailOF> getDetailsByOfId(Long ofId) {
        OrdreFabrication of = ordreFabricationRepository.findById(ofId)
                .orElseThrow(() -> new RuntimeException("Ordre de Fabrication non trouvé"));
        return detailOFRepository.findByOrdreFabrication(of);
    }

    // Livrer un OF ou ses sous-OF
    public void deliverOfOrSubOf(DeliveryRequest deliveryRequest) {
        if (deliveryRequest.getSousOfIds() != null && !deliveryRequest.getSousOfIds().isEmpty()) {
            // Livraison des sous-OF
            for (Long sousOfId : deliveryRequest.getSousOfIds()) {
                DetailOF detail = detailOFRepository.findById(sousOfId)
                        .orElseThrow(() -> new RuntimeException("Sous-OF non trouvé"));
                detail.setDelivered(true); // Exemple : marquez comme livré
                detailOFRepository.save(detail);
            }
        } else {
            // Livraison de l'OF principal
            OrdreFabrication of = ordreFabricationRepository.findById(deliveryRequest.getOfId())
                    .orElseThrow(() -> new RuntimeException("Ordre de Fabrication non trouvé"));
            of.setDelivered(true); // Exemple : marquez comme livré
            ordreFabricationRepository.save(of);
        }
    }*/
}
