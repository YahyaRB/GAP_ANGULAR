package ma.gap.service;

import ma.gap.dtos.NomenclatureDTO;
import ma.gap.dtos.NomenclatureQteRestDto;
import ma.gap.entity.Nomenclature;
import ma.gap.entity.OrdreFabrication;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface NomenclatureService {
    public List<Nomenclature> findByOrdreFabrication(OrdreFabrication ordreFabrication) ;

    public List<Nomenclature> findByOrdreFabricationId(Long ordreFabricationId) ;

    public Nomenclature save(Nomenclature nomenclature,long idof);

    public Optional<Nomenclature> findById(Long id) ;

    public void deleteById(Long id) ;

    public List<Nomenclature> findAll() ;

    // Méthode pour calculer la quantité restante d'une nomenclature
    public double getQuantiteRestante(Long nomenclatureId) ;

    // Méthode pour mettre à jour les quantités après livraison
    public void updateQuantitiesAfterDelivery(Long nomenclatureId, double quantiteLivree) ;
    // Récupérer les nomenclatures par projet avec quantités
    public List<NomenclatureQteRestDto> getNomenclaturesByProjetWithQuantities(Long projetId);

    // Récupérer les nomenclatures par OF avec quantités
    public List<NomenclatureQteRestDto> getNomenclaturesByOFWithQuantities(Long ofId) ;
    // Mapper vers DTO
    public List<NomenclatureQteRestDto> mapToNomenclatureQteRestDto(List<Nomenclature> nomenclatures) ;
    public NomenclatureQteRestDto mapToDto(Nomenclature nomenclature) ;




    public NomenclatureDTO getNomenclatureById(Long id);
    public List<NomenclatureDTO> getNomenclaturesByOF(Long ofId);

}
