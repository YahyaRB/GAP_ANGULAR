package ma.gap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.gap.entity.ArticleAch;
import ma.gap.entity.Nomenclature;
import ma.gap.entity.NomenclatureArticleAch;
import ma.gap.entity.Plan;


@Repository
public interface NomenclatureRepository extends JpaRepository<Nomenclature, Long> {
    List<Nomenclature> findByNumeroPlan_Id(Long idPlan);
	//List<Nomenclature> findAllByArticleAch(ArticleAch articleAch);
//	List<Nomenclature> findAllByArticleAch(ArticleAch articleAch, NomenclatureArticleAch nomenclatureArticleAch);
}
