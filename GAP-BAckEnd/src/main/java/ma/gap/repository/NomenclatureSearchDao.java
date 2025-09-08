package ma.gap.repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ma.gap.service.UserImpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import ma.gap.entity.Nomenclature;
import ma.gap.entity.NomenclatureArticleAch;

@Repository
@RequiredArgsConstructor
public class NomenclatureSearchDao {
	private final EntityManager em ;
	 @Autowired
	 UserImpService userImpService;
	 @Autowired 
	 private ArticleAchRepository articleAchRepository;
	 @Autowired
	 private PlanRepository planRepository;

	 public List<Nomenclature> searchNomenclature(Long idArticleAch, Long idPlan, Long idNomenclature) throws ParseException {
		    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		    CriteriaQuery<Nomenclature> criteriaQuery = criteriaBuilder.createQuery(Nomenclature.class);
		    List<Predicate> predicates = new ArrayList<>();
		    Root<Nomenclature> root = criteriaQuery.from(Nomenclature.class);

		    if (idArticleAch != null && idArticleAch != 0) {
		        Join<Nomenclature, NomenclatureArticleAch> articleAchJoin = root.join("nomenclatureArticleAch");
		        Predicate articlePredicate = criteriaBuilder.equal(articleAchJoin.get("articleAch").get("id"), idArticleAch);
		        predicates.add(articlePredicate);
		    }

		    if (idPlan != null && idPlan != 0) {
		        Predicate planPredicate = criteriaBuilder.equal(root.get("numeroPlan").get("id"), idPlan);
		        predicates.add(planPredicate);
		    }

		    if (idNomenclature != null && idNomenclature != 0) {
		        Predicate nomenclaturePredicate = criteriaBuilder.equal(root.get("id"), idNomenclature);
		        predicates.add(nomenclaturePredicate);
		    }

		    criteriaQuery.where(predicates.toArray(new Predicate[0]));
		    TypedQuery<Nomenclature> query = em.createQuery(criteriaQuery);
		    return query.getResultList();
		}


}
