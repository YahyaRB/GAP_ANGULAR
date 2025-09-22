package ma.gap.controller;

import ma.gap.dtos.NomenclatureQteRestDto;
import ma.gap.entity.Nomenclature;
import ma.gap.entity.OrdreFabrication;
import ma.gap.repository.NomenclatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NomenclatureService {

    @Autowired
    private NomenclatureRepository nomenclatureRepository;

    @Autowired
    private NomenclatureRepository nomenclatureArticleAchRepository;

    public List<Nomenclature> findByOrdreFabrication(OrdreFabrication ordreFabrication) {
        return nomenclatureRepository.findByOrdreFabrication(ordreFabrication);
    }

    public List<Nomenclature> findByOrdreFabricationId(Long ordreFabricationId) {
        return nomenclatureRepository.findByOrdreFabricationId(ordreFabricationId);
    }

    public Nomenclature save(Nomenclature nomenclature) {
        return nomenclatureRepository.save(nomenclature);
    }

    public Optional<Nomenclature> findById(Long id) {
        return nomenclatureRepository.findById(id);
    }

    public void deleteById(Long id) {
        nomenclatureRepository.deleteById(id);
    }

    public List<Nomenclature> findAll() {
        return nomenclatureRepository.findAll();
    }

    // Méthode pour calculer la quantité restante d'une nomenclature
    public double getQuantiteRestante(Long nomenclatureId) {
        Optional<Nomenclature> nomenclature = findById(nomenclatureId);
        if (nomenclature.isPresent()) {
            Nomenclature nom = nomenclature.get();
            return nom.getQuantiteRest();
        }
        return 0.0;
    }

    // Méthode pour mettre à jour les quantités après livraison
    public void updateQuantitiesAfterDelivery(Long nomenclatureId, double quantiteLivree) {
        Optional<Nomenclature> nomenclatureOpt = findById(nomenclatureId);
        if (nomenclatureOpt.isPresent()) {
            Nomenclature nomenclature = nomenclatureOpt.get();
            nomenclature.setQuantiteLivre(nomenclature.getQuantiteLivre() + quantiteLivree);
            nomenclature.setQuantiteRest(nomenclature.getQuantiteTot() - nomenclature.getQuantiteLivre());
            save(nomenclature);
        }
    }

    // Récupérer les nomenclatures par projet avec quantités
    public List<NomenclatureQteRestDto> getNomenclaturesByProjetWithQuantities(Long projetId) {
        List<Nomenclature> nomenclatures = nomenclatureRepository.findAvailableNomenclaturesByProjetId(projetId);
        return mapToNomenclatureQteRestDto(nomenclatures);
    }

    // Récupérer les nomenclatures par OF avec quantités
    public List<NomenclatureQteRestDto> getNomenclaturesByOFWithQuantities(Long ofId) {
        List<Nomenclature> nomenclatures = nomenclatureRepository.findAvailableNomenclaturesByOfId(ofId);
        return mapToNomenclatureQteRestDto(nomenclatures);
    }

    // Mapper vers DTO
    private List<NomenclatureQteRestDto> mapToNomenclatureQteRestDto(List<Nomenclature> nomenclatures) {
        return nomenclatures.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private NomenclatureQteRestDto mapToDto(Nomenclature nomenclature) {
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

    /**
     * Méthode pour créer une nomenclature avec ses articles d'achat associés
     * Cette méthode est appelée par le NomenclatureController existant
     */
    @Transactional
    public Nomenclature createNomenclature(Nomenclature nomenclature, List<Nomenclature> article) {
        try {
            // Sauvegarder d'abord la nomenclature
            Nomenclature savedNomenclature = save(nomenclature);

            // Si il y a des articles d'achat associés, les traiter
            if (article != null && !article.isEmpty()) {
                for (Nomenclature art : article) {
                    // Associer l'article à la nomenclature sauvegardée
                    art.setNomenclature(savedNomenclature);
                    // Sauvegarder l'article d'achat
                    nomenclatureArticleAchRepository.save(art);
                }
            }

            return savedNomenclature;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de la nomenclature: " + e.getMessage(), e);
        }
    }

    /**
     * Méthode alternative si vous voulez juste créer une nomenclature simple
     */
    public Nomenclature createSimpleNomenclature(Nomenclature nomenclature) {
        return save(nomenclature);
    }
}