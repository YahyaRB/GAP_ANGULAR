package ma.gap.repository;

import ma.gap.entity.Article;
import ma.gap.entity.Ateliers;
import ma.gap.entity.OrdreFabrication;
import ma.gap.entity.Plan;
import ma.gap.entity.Projet;
import ma.gap.enums.StatutPlan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
	   List<Plan> findAllByAffaire(Projet affaire);
	    List<Plan> findAllByAtelierOrderByIdDesc(Ateliers ateliers);
	    List<Plan> findAllByAtelierInOrderByIdDesc(List<Ateliers> ateliers);
	    List<Plan> findAllByAffaireAndAtelierOrderByIdAsc(Projet affaire, Ateliers atelier);
	
	
	List<Plan> findByStatut(StatutPlan VALIDÉ);

	List<Plan> findByArticleAndAtelier(Article article,Ateliers atelier);




}

