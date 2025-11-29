package ma.gap.repository;

import ma.gap.entity.Article;
import ma.gap.entity.Ateliers;
import ma.gap.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAllByAteliersOrderByIdDesc(Ateliers atelier, Pageable pageable);

    List<Article> findAllByProjetAndAteliersOrderByIdDesc(long projet, long atelier);

    Set<Article> findAllByAteliersOrderByDesignationAsc(Ateliers atelier);

    List<Article> findAllByProjetAndAteliersInOrderByIdDesc(Projet projet, List<Ateliers> ateliers);

    List<Article> findAllByProjet(Projet projet);

    /*
     * public List<Article> findAllByprojetAndateliers(Projet projet, Ateliers
     * atelier);
     */
    List<Article> findAllByProjetAndAteliersOrderByDesignationAsc(Projet projet, Ateliers atelier);

    List<Article> findAllByAteliersOrderByIdDesc(long atelier);

    @Query(value = "SELECT art.* " +
            "FROM article art " +
            "JOIN ateliers at ON at.id = art.ateliers_id " +
            "JOIN projet p ON p.id = art.projet_id " +
            "WHERE art.quantite_tot > (" +
            "    SELECT COUNT(o.quantite) " +
            "    FROM ordre_fabrication o " +
            "    WHERE o.article_id = art.id" +
            ") " +
            "AND art.projet_id = :projetId " +
            "AND art.ateliers_id = :atelierId", nativeQuery = true)
    List<Article> findArticlesByProjetIdAndAtelierId(@Param("projetId") Long projetId,
            @Param("atelierId") Long atelierId);

}
