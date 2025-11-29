package ma.gap.repository;

import ma.gap.entity.Projet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    Page<Projet> findByCodeContainingOrDesignationContaining(String code, String designation, Pageable pageable);

    // List<Projet> findAllByAteliers(List<Ateliers> ateliers);
    Projet findByCode(String code);

    List<Projet> findAllByStatus(int status);

    @Query("SELECT DISTINCT a FROM Projet a, OrdreFabrication odf WHERE a.id=odf.projet.id and " +
            "odf.atelier.id = :atelierId order by a.id desc")
    List<Projet> findAffairesByAtelierAndOF(@Param("atelierId") long atelierId);

    /*
     * @Query(value ="SELECT DISTINCT p.* FROM projet p " +
     * "JOIN article art ON p.id = art.projet_id " +
     * "JOIN ateliers at ON at.id = art.ateliers_id " +
     * "WHERE at.id =:atelier_id AND art.quantite_tot > " +
     * "( SELECT COUNT(o.quantite) FROM ordre_fabrication o " +
     * "WHERE o.article_id = art.id) AND p.status=2"
     * , nativeQuery = true)
     * List<Projet>
     * findAffairesByAtelierAndQteArticle_Sup_QteOF(@Param("atelier_id") long
     * atelier_id);
     * }
     */

    @Query(value = "SELECT DISTINCT p.* FROM projet p " +
            "JOIN article art ON p.id = art.projet_id " +
            "JOIN ateliers a ON a.id = art.ateliers_id " +
            "WHERE a.id = :atelier_id  AND art.quantite_tot > art.quantite_livre AND p.status=2 " +
            "AND EXISTS (SELECT 1 FROM ordre_fabrication ofa WHERE ofa.article_id = art.id)", nativeQuery = true)
    List<Projet> findAffairesByAtelierAndQteArticle_Sup_QteEnProd(@Param("atelier_id") long atelier_id);
}
