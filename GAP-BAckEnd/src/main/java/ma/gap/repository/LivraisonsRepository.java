package ma.gap.repository;

import ma.gap.entity.Ateliers;
import ma.gap.entity.Projet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ma.gap.entity.Livraisons;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public interface LivraisonsRepository extends JpaRepository<Livraisons, Long> {
 EntityManager em = null;
    List<Livraisons> findAllByProjetOrderByIdDesc(Projet projet);
    List<Livraisons>findAllByAtelierOrderByIdDesc(Ateliers ateliers);
    List<Livraisons>findAllByAtelierInOrderByIdDesc(List<Ateliers> ateliers);

    @Query(value = "SELECT l FROM Livraisons l WHERE CONCAT(l.atelier.id, '') LIKE %:at%", nativeQuery = true)
    public List<Livraisons> search(@Param("at") String at);
   boolean existsByChauffeurId(Long chauffeurId);

    @Query("SELECT l FROM Livraisons l WHERE l.atelier IN :ateliers")
    List<Livraisons> findAllByAtelier(@Param("ateliers") List<Ateliers> ateliers);

    @Query("SELECT DISTINCT l.projet FROM Livraisons l WHERE l.atelier.id = :atelierId")
    List<Projet> findAffairesWithLivraisonByAtelier(@Param("atelierId") Long atelierId);

}



