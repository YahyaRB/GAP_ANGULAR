package ma.gap.repository;

import ma.gap.entity.Nomenclature;
import ma.gap.entity.OrdreFabrication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NomenclatureRepository extends JpaRepository<Nomenclature, Long> {

    List<Nomenclature> findByOrdreFabrication(OrdreFabrication ordreFabrication);

    List<Nomenclature> findByOrdreFabricationId(Long ordreFabricationId);

    @Query("SELECT n FROM Nomenclature n WHERE n.ordreFabrication.id = :ofId AND n.quantiteRest > 0")
    List<Nomenclature> findAvailableNomenclaturesByOfId(@Param("ofId") Long ofId);

    @Query("SELECT n FROM Nomenclature n WHERE n.ordreFabrication.projet.id = :projetId AND n.quantiteRest > 0")
    List<Nomenclature> findAvailableNomenclaturesByProjetId(@Param("projetId") Long projetId);

    @Query("SELECT COUNT(n) FROM Nomenclature n WHERE n.ordreFabrication.id = :ofId")
    Long countByOrdreFabricationId(@Param("ofId") Long ofId);

    @Query("SELECT SUM(n.quantiteRest) FROM Nomenclature n WHERE n.ordreFabrication.id = :ofId")
    Double getTotalQuantiteRestByOfId(@Param("ofId") Long ofId);
    List<Nomenclature> findByOrdreFabricationOrderByIdDesc(OrdreFabrication ordreFabrication);


    @Query("SELECT n FROM Nomenclature n WHERE n.ordreFabrication.id = :ofId AND n.quantiteRest > 0")
    List<Nomenclature> findAvailableByOrdreFabricationId(@Param("ofId") Long ofId);
}














