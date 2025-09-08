package ma.gap.repository;

import ma.gap.entity.Historique;
import ma.gap.entity.Plan;
import ma.gap.enums.TypeHistorique;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface HistoriqueRepository extends JpaRepository<Historique,Long> {

	  List<Historique> findByNumeroPlanIdOrderByDate(Long planId);

	Historique findByNumeroPlanAndType(Plan updatedPlan, TypeHistorique création);

	List<Historique> findByNumeroPlan(Plan plan);
	
	Optional<Historique> findById(Long id);
	 Historique findTopByNumeroPlanOrderByDateDesc(Plan plan);

	void deleteByNumeroPlan(Plan planToDelete);


	Historique findFirstByNumeroPlanOrderByDateDesc(Plan plan);



	 List<Historique> findByNumeroPlanIdOrderByDateDesc(Long numeroPlanId);
}

