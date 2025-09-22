package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.dtos.NomenclatureDTO;
import ma.gap.dtos.NomenclatureQteRestDto;
import ma.gap.entity.Nomenclature;
import ma.gap.entity.OrdreFabrication;
import ma.gap.repository.NomenclatureRepository;
import ma.gap.repository.OrdreFabricationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
@AllArgsConstructor
@Service
public class NomenclatureImpService implements NomenclatureService{

    private NomenclatureRepository nomenclatureRepository;
    private OrdreFabricationRepository ordreFabricationRepository;


    @Override
    public List<Nomenclature> findByOrdreFabrication(OrdreFabrication ordreFabrication) {
        return nomenclatureRepository.findByOrdreFabrication(ordreFabrication);
    }
    @Override
    public List<Nomenclature> findByOrdreFabricationId(Long ordreFabricationId) {
        return nomenclatureRepository.findByOrdreFabricationId(ordreFabricationId);
    }
    @Override
    public Nomenclature save(Nomenclature nomenclature,long idof) {
        OrdreFabrication of= ordreFabricationRepository.findById(idof).get();
        nomenclature.setOrdreFabrication(of);
        return nomenclatureRepository.save(nomenclature);
    }
    @Override
    public Optional<Nomenclature> findById(Long id) {
        return nomenclatureRepository.findById(id);
    }
    @Override
    public void deleteById(Long id) {
        nomenclatureRepository.deleteById(id);
    }
    @Override
    public List<Nomenclature> findAll() {
        return nomenclatureRepository.findAll();
    }

    // Méthode pour calculer la quantité restante d'une nomenclature
    @Override
    public double getQuantiteRestante(Long nomenclatureId) {
        Optional<Nomenclature> nomenclature = findById(nomenclatureId);
        if (nomenclature.isPresent()) {
            Nomenclature nom = nomenclature.get();
            return nom.getQuantiteRest();
        }
        return 0.0;
    }

    // Méthode pour mettre à jour les quantités après livraison
    @Override
    public void updateQuantitiesAfterDelivery(Long nomenclatureId, double quantiteLivree) {
        Optional<Nomenclature> nomenclatureOpt = findById(nomenclatureId);
        if (nomenclatureOpt.isPresent()) {
            Nomenclature nomenclature = nomenclatureOpt.get();
            nomenclature.setQuantiteLivre(nomenclature.getQuantiteLivre() + quantiteLivree);
            nomenclature.setQuantiteRest(nomenclature.getQuantiteTot() - nomenclature.getQuantiteLivre());
            save(nomenclature,nomenclature.getOrdreFabrication().getId());
        }
    }

    // Récupérer les nomenclatures par projet avec quantités
    @Override
    public List<NomenclatureQteRestDto> getNomenclaturesByProjetWithQuantities(Long projetId) {
        List<Nomenclature> nomenclatures = nomenclatureRepository.findAvailableNomenclaturesByProjetId(projetId);
        return mapToNomenclatureQteRestDto(nomenclatures);
    }

    // Récupérer les nomenclatures par OF avec quantités
    @Override
    public List<NomenclatureQteRestDto> getNomenclaturesByOFWithQuantities(Long ofId) {
        List<Nomenclature> nomenclatures = nomenclatureRepository.findAvailableNomenclaturesByOfId(ofId);
        return mapToNomenclatureQteRestDto(nomenclatures);
    }

    // Mapper vers DTO
    @Override
    public List<NomenclatureQteRestDto> mapToNomenclatureQteRestDto(List<Nomenclature> nomenclatures) {
        return nomenclatures.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Override
    public NomenclatureQteRestDto mapToDto(Nomenclature nomenclature) {
        NomenclatureQteRestDto dto = new NomenclatureQteRestDto();
        dto.setId(nomenclature.getId());
        dto.setType(nomenclature.getType());
        dto.setUnite(nomenclature.getUnite());
        dto.setQuantiteTot(nomenclature.getQuantiteTot());
        dto.setQuantiteLivre(nomenclature.getQuantiteLivre());
        dto.setQuantiteRest(nomenclature.getQuantiteRest());

        if (nomenclature.getOrdreFabrication() != null) {
            dto.setOrdreFabricationId(nomenclature.getOrdreFabrication().getId());
            dto.setNumOF(nomenclature.getOrdreFabrication().getNumOF());

            if (nomenclature.getOrdreFabrication().getArticle() != null) {
                dto.setArticleDesignation(nomenclature.getOrdreFabrication().getArticle().getDesignation());
            }
        }

        return dto;
    }







    @Override
    public List<NomenclatureDTO> getNomenclaturesByOF(Long ofId) {
        try {
            // Vérifier que l'OF existe
            Optional<OrdreFabrication> ofOptional = ordreFabricationRepository.findById(ofId);
            if (!ofOptional.isPresent()) {
                System.out.println("Ordre de fabrication non trouvé avec l'ID: " + ofId);
                return new ArrayList<>();
            }

            OrdreFabrication of = ofOptional.get();

            // Récupérer les nomenclatures liées à cet OF
            List<Nomenclature> nomenclatures = nomenclatureRepository.findByOrdreFabricationOrderByIdDesc(of);

            // Convertir en DTO
            List<NomenclatureDTO> nomenclaturesDTO = nomenclatures.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            System.out.println("Nomenclatures trouvées pour OF " + of.getNumOF() + ": " + nomenclaturesDTO.size());

            return nomenclaturesDTO;

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des nomenclatures pour OF " + ofId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Récupère une nomenclature par son ID
     * @param id ID de la nomenclature
     * @return NomenclatureDTO ou null si non trouvée
     */
    @Override
    public NomenclatureDTO getNomenclatureById(Long id) {
        try {
            Optional<Nomenclature> nomenclatureOptional = nomenclatureRepository.findById(id);

            if (nomenclatureOptional.isPresent()) {
                return convertToDTO(nomenclatureOptional.get());
            } else {
                System.out.println("Nomenclature non trouvée avec l'ID: " + id);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la nomenclature " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convertit une entité Nomenclature en DTO
     * @param nomenclature L'entité à convertir
     * @return Le DTO correspondant
     */
    private NomenclatureDTO convertToDTO(Nomenclature nomenclature) {
        NomenclatureDTO dto = new NomenclatureDTO();

        dto.setId(nomenclature.getId());
        dto.setType(String.valueOf(nomenclature.getType()));
        dto.setDesignation(nomenclature.getDesignation());
        dto.setUnite(nomenclature.getUnite());
        dto.setQuantite(nomenclature.getQuantiteTot());
        dto.setQuantiteRest(nomenclature.getQuantiteRest());
        dto.setQuantiteLivre(nomenclature.getQuantiteLivre());

        // Récupérer l'ID de l'OF parent
        if (nomenclature.getOrdreFabrication() != null) {
            dto.setOrdreFabricationId(nomenclature.getOrdreFabrication().getId());
        }

        return dto;
    }

    /**
     * Méthode pour récupérer le résumé des nomenclatures (optionnel)
     * @param ofId ID de l'ordre de fabrication
     * @return Statistiques des nomenclatures
     */
    public Map<String, Object> getNomenclaturesSummary(Long ofId) {
        List<NomenclatureDTO> nomenclatures = getNomenclaturesByOF(ofId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", nomenclatures.size());
        summary.put("disponibles", nomenclatures.stream()
                .mapToInt(n -> n.getQuantiteRest() > 0 ? 1 : 0)
                .sum());
        summary.put("epuisees", nomenclatures.stream()
                .mapToInt(n -> n.getQuantiteRest() <= 0 ? 1 : 0)
                .sum());
        summary.put("quantiteTotale", nomenclatures.stream()
                .mapToDouble(n -> n.getQuantite() != null ? n.getQuantite() : 0.0)
                .sum());
        summary.put("quantiteRestante", nomenclatures.stream()
                .mapToDouble(n -> n.getQuantiteRest() != null ? n.getQuantiteRest() : 0.0)
                .sum());

        return summary;
    }
}
