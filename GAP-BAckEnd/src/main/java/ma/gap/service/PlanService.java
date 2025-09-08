package ma.gap.service;

import ma.gap.entity.*;
import ma.gap.dtos.HistoriqueDTO;
import ma.gap.exceptions.PlanNotFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface PlanService {

    Plan createPlan(Plan plan ) throws IOException;
    

    
    Plan validatePlan(Long planId,  User controlPar) throws PlanNotFoundException;
    
    List<Plan> getAllPlans();

    Optional<Plan> findPlanById(long id);

    void deletePlan(long id) throws IOException, PlanNotFoundException, EmptyResultDataAccessException;

    Optional<Article> getArticlesByAtelier(Long atelierId);
    
    public Plan editPlan(Plan plan,Long id);

	ResponseEntity<byte[]> telechargerPieceJointe(Long planId) throws PlanNotFoundException, FileNotFoundException;

	Optional<Historique> findById(Long planId);

	List<HistoriqueDTO> getHistoriqueByPlanId(Long planId);

	List<Plan> getValidPlans();

	

	List<Plan> findByArticleAndAtelier(Article article,Ateliers atelier);


	void deletePlanAndAssociatedHistoriques(Long planId) throws PlanNotFoundException;

	List<Plan> findAllByAtelier(long idUser);

	Page<Plan> SearchofByProjectAndAtelier(Pageable pageable, User user, long idprojet, long atelier)
			throws ParseException;

	Page<Plan> planByProjectAndAtelier(Pageable pageable, Projet affaire, Ateliers atelier);

	Page<Plan> planByProject(Pageable pageable, Projet affaire);



	Plan deposerPlan(Long planId, MultipartFile file, User dessinePar, String indice) throws PlanNotFoundException;



	Plan findPlanById(Long planId) throws PlanNotFoundException;

//	String getNextIndice(String currentIndice);


	

	
}
