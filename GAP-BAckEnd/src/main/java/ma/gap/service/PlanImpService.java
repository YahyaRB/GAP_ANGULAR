package ma.gap.service;

import ma.gap.dtos.HistoriqueDTO;
import ma.gap.entity.*;
import ma.gap.repository.*;
import org.springframework.beans.factory.annotation.Value;
import ma.gap.config.GlobalVariableConfig;

import ma.gap.enums.StatutPlan;
import ma.gap.enums.TypeHistorique;
import ma.gap.exceptions.PlanNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

@Service

public class PlanImpService implements PlanService {
//	private static final Logger logger = Logger.getLogger(PlanImpService.class);

	//PlanSearchDoa planSearchDoa;
	   @Autowired
	   private PlanRepository planRepository;
	   @Autowired
	   private ArticleRepository articleRepository;
	   @Autowired
	   private HistoriqueRepository historiqueRepository;
	   @Autowired
	   private PlanSearchDoa planSearchDoa;
	   @Autowired
	   private UserImpService userImpService;
	   private GlobalVariableConfig globalVariableConfig;
	    
	    private String getUsername(User user) {
	        return user != null ? user.getUsername() : null;
	    }
	    @Value("${app.path.globalVariable}")
	    private String globalPath;
	    
// create plan    
    @Override
    public Plan createPlan(Plan plan ) throws IOException {
    	plan.setStatut(StatutPlan.BROUILLON);
    	Plan savedPlan =planRepository.save(plan);
    	 Historique historique = new Historique();
    	    historique.setType(TypeHistorique.Création);
    	    historique.setNumeroPlan(savedPlan);
    	    historique.setDate(new Date());
    	    historiqueRepository.save(historique);
         return savedPlan;
    }
// fin create plan   
 
    
    
    
  // deposer plan sans enregistrement de historique   
    
//    @Override
//    public Plan deposerPlan(Long planId, MultipartFile file, Login dessinePar) throws PlanNotFoundException {
//        Optional<Plan> optionalPlan = planRepository.findById(planId);
//        if (optionalPlan.isPresent()) {
//            Plan plan = optionalPlan.get();
//            plan.setStatut(StatutPlan.EN_COURS);
//            plan.setDessinePar(dessinePar); 
//            
//            if (file != null && !file.isEmpty()) {
//                try {
//                    String fileName = file.getOriginalFilename();
//                    String filePath = "C:\\Users\\f.ali\\Plan\\" + fileName; 
//                    File saveFile = new File(filePath);
//                    file.transferTo(saveFile);
//                    plan.setPieceJointe(filePath); 
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return planRepository.save(plan);
//        } else {
//            throw new PlanNotFoundException("Plan not found with id: " + planId);
//        }
//    }

    // fin deposer plan sans enregistrement de historique
    
    
    
    
// deposer plan avec enregistrement de historique 
    @Override
    public Plan deposerPlan(Long planId, MultipartFile file, User dessinePar, String indice) throws PlanNotFoundException {
        Optional<Plan> optionalPlan = planRepository.findById(planId);
        if (optionalPlan.isPresent()) {
            Plan plan = optionalPlan.get();
            plan.setStatut(StatutPlan.EN_COURS);
            plan.setDessinePar(dessinePar); 
            
            if (file != null && !file.isEmpty()) {
                try {
                    String originalFileName = file.getOriginalFilename();
                    String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    String fileName = UUID.randomUUID().toString() + fileExtension;
                    String filePath = globalPath + "\\" + fileName; 
                    File saveFile = new File(filePath);
                    file.transferTo(saveFile);
                    plan.setPieceJointe(fileName); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            Plan savedPlan = planRepository.save(plan);
            if (dessinePar != null) {
                Historique historique = new Historique();
                historique.setNumeroPlan(savedPlan);
                historique.setType(TypeHistorique.Deposer);  
                historique.setDate(new Date());
                historique.setFaitPar(dessinePar);
                historique.setIndice(indice); 
                historique.setValidePar(null); 
                historiqueRepository.save(historique);
            }
            return savedPlan;
        } else {
            throw new PlanNotFoundException("Plan not found with id: " + planId);
        }
    }
    @Override
    public Plan findPlanById(Long planId) throws PlanNotFoundException {
        return planRepository.findById(planId).orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + planId));
    }

//  fin  deposer plan avec enregistrement de historique
    
    
    
    
    
    
 // telecharger PieceJointe

    public ResponseEntity<byte[]> telechargerPieceJointe(Long planId) throws PlanNotFoundException, FileNotFoundException {
        Optional<Plan> optionalPlan = planRepository.findById(planId);
        if (optionalPlan.isPresent()) { 
            Plan plan = optionalPlan.get();
            String fileName = plan.getPieceJointe();
            if (fileName != null && !fileName.isEmpty()) {
                String filePath = globalPath + "\\" + fileName; 
                File file = new File(filePath);
                try (InputStream inputStream = new FileInputStream(file)) {
                    byte[] bytes = new byte[(int) file.length()];
                    inputStream.read(bytes);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDispositionFormData("attachment", file.getName());
                    headers.setContentLength(bytes.length);
                    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
                } catch (IOException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                throw new FileNotFoundException("File not found for plan id: " + planId);
            }
        } else {
            throw new PlanNotFoundException("Plan not found with id: " + planId);
        }
    }
 // fin telecharger PieceJointe   
    

    
    
    
// validation Plan  avec  enregistrement de historique   
    public Plan validatePlan(Long planId, User controlPar) throws PlanNotFoundException {
        Optional<Plan> optionalPlan = planRepository.findById(planId);
        if (optionalPlan.isPresent()) {
            Plan plan = optionalPlan.get();
            
            Historique lastHistorique = historiqueRepository.findFirstByNumeroPlanOrderByDateDesc(plan);
            String lastIndice = lastHistorique != null ? lastHistorique.getIndice() : null;
            
            String indice = lastIndice != null ? lastIndice : "A";
            
            Historique historique = new Historique();
            historique.setNumeroPlan(plan);
            historique.setType(TypeHistorique.Validation);
            historique.setDate(new Date());
            historique.setFaitPar(null); 
            historique.setValidePar(controlPar);
            historique.setIndice(indice);
            
            historiqueRepository.save(historique);

            plan.setStatut(StatutPlan.VALIDÉ);
            plan.setControlPar(controlPar);
            Plan validatedPlan = planRepository.save(plan);
            
            return validatedPlan;
        } else {
            throw new PlanNotFoundException("Plan not found with id: " + planId);
        }
    }
 // fin validation Plan  avec  enregistrement de historique       
    
    

    
    
   
// affiche tous les plans 
    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
    }
// fin affiche tous les plans
    
    
    
    @Override
    public List<Plan> getValidPlans() {
        return planRepository.findByStatut(StatutPlan.VALIDÉ);
    }
    
    
    
// filtre plan par id     

// fin filtre plan par id 
    
    
    

    
// delete plan 
    @Override
	public void deletePlan(long id) throws IOException,PlanNotFoundException,EmptyResultDataAccessException{
		Optional<Plan> plans = planRepository.findById(id);
			planRepository.deleteById(id);
	}
    

    
    @Transactional
    public void deletePlanAndAssociatedHistoriques(Long planId) throws PlanNotFoundException {
        Plan planToDelete = planRepository.findById(planId)
            .orElseThrow(() -> new PlanNotFoundException("Le plan avec l'ID " + planId + " n'existe pas"));

        historiqueRepository.deleteByNumeroPlan(planToDelete);
        planRepository.delete(planToDelete);
    }
    
// fin delete plan 
    
    
    
    
    
    
// editer Plan 
    @Override
    public Plan editPlan(Plan plan, Long id) {
        plan.setId(id);
        Plan updatedPlan = planRepository.save(plan);
        
        // Retrieve the last indice value
        Historique lastHistorique = historiqueRepository.findFirstByNumeroPlanOrderByDateDesc(updatedPlan);
        String lastIndice = (lastHistorique != null) ? lastHistorique.getIndice() : "None";
        
        Historique historique = new Historique();
        historique.setType(TypeHistorique.Modification);
        historique.setNumeroPlan(updatedPlan);
        historique.setDate(new Date());
        historique.setIndice(lastIndice);
        
        historiqueRepository.save(historique);
        
        return updatedPlan;
    }

    @Override
    public Optional<Plan> findPlanById(long id) {
        return planRepository.findById(id);
    }
// fin editer plan
    
    
    
    

    
    
    

    

    
    
// filtre article par atelier    
    @Override
    public Optional<Article> getArticlesByAtelier(Long atelierId) {
        return articleRepository.findById(atelierId);
    }
 // fin filtre article par atelier      
    

    
    
    
    
   
 // filtre plan par id      
	@Override
	public Optional<Historique> findById(Long planId) {
		return historiqueRepository.findById(planId);
	}
// filtre plan par id  
	
	
	
	
	
	
	
// afficher historique de plan  	
	@Override
	public List<HistoriqueDTO> getHistoriqueByPlanId(Long planId) {
	    List<Historique> historiques = historiqueRepository.findByNumeroPlanIdOrderByDate(planId);
	    List<HistoriqueDTO> historiqueDTOs = new ArrayList<>();

	    for (Historique historique : historiques) {
	        HistoriqueDTO historiqueDTO = new HistoriqueDTO();
	        historiqueDTO.setId(historique.getId());
	        historiqueDTO.setDate(historique.getDate());
	        historiqueDTO.setTypeHistorique(historique.getType().toString());
	        historiqueDTO.setFaitPar(historique.getNumeroPlan().getCreatedBy().toString());
	        historiqueDTO.setValidePar(getUsername(historique.getNumeroPlan().getControlPar()));
	        historiqueDTO.setEvenement(getEvenement(historique.getType())); 
	        historiqueDTOs.add(historiqueDTO); 
	    }
	    return historiqueDTOs;
	}

	private String getEvenement(TypeHistorique type) {
	    switch (type) {
	        case Création:
	            return "Création de plan";
	        case Modification:
	            return "Modification de plan";
	        case Validation:
	            return "Validation de plan";
	        case Deposer:
	            return "Deposer de plan";
	        default:
	            return "Type d'historique inconnu";
	    }
	}
// fin afficher historique de plan 
	
	
	
	@Override
	public List<Plan> findByArticleAndAtelier(Article article,Ateliers atelier) {
	     return planRepository.findByArticleAndAtelier(article,atelier);
	    }



	public void setGlobalVariableConfig(GlobalVariableConfig globalVariableConfig) {
		this.globalVariableConfig = globalVariableConfig;
	}
	
	
	 @Override
	    public Page<Plan> planByProject(Pageable pageable, Projet affaire) {
	        int pageSize = pageable.getPageSize();
	        int currentPage = pageable.getPageNumber();
	        int startItem = currentPage * pageSize;
	        List<Plan> listCons;
	        List<Plan> listePlans = planRepository.findAllByAffaire(affaire);

	        if (listePlans.size() < startItem) {
	            listCons = Collections.emptyList();
	        } else {
	            int toIndex = Math.min(startItem + pageSize, listePlans.size());
	            listCons = listePlans.subList(startItem, toIndex);
	        }
	        return new PageImpl<>(listCons, PageRequest.of(currentPage, pageSize), listePlans.size());
	    }

	    @Override
	    public Page<Plan> planByProjectAndAtelier(Pageable pageable, Projet affaire, Ateliers atelier) {
	        int pageSize = pageable.getPageSize();
	        int currentPage = pageable.getPageNumber();
	        int startItem = currentPage * pageSize;
	        List<Plan> listCons;
	        List<Plan> listePlans = planRepository.findAllByAffaireAndAtelierOrderByIdAsc(affaire, atelier);

	        if (listePlans.size() < startItem) {
	            listCons = Collections.emptyList();
	        } else {
	            int toIndex = Math.min(startItem + pageSize, listePlans.size());
	            listCons = listePlans.subList(startItem, toIndex);
	        }
	        return new PageImpl<>(listCons, PageRequest.of(currentPage, pageSize), listePlans.size());
	    }

	    @Override
	    public Page<Plan> SearchofByProjectAndAtelier(Pageable pageable, User user, long idprojet, long atelier) throws ParseException {
	        int pageSize = pageable.getPageSize();
	        int currentPage = pageable.getPageNumber();
	        int startItem = currentPage * pageSize;
	        List<Plan> listCons;
	        List<Plan> listePlans = planSearchDoa.searchPlanByProjet(user, idprojet, atelier);

	        if (listePlans.size() < startItem) {
	            listCons = Collections.emptyList();
	        } else {
	            int toIndex = Math.min(startItem + pageSize, listePlans.size());
	            listCons = listePlans.subList(startItem, toIndex);
	        }
	        return new PageImpl<>(listCons, PageRequest.of(currentPage, pageSize), listePlans.size());
	    }

	    @Override
	    public List<Plan> findAllByAtelier(long idUser) {
	        List<Plan> listePlans = new ArrayList<>();
	        List<Ateliers> at = new ArrayList<>();
	        User login = userImpService.findbyusername(idUser);

	        for (Role role : login.getRoles()) {
	            if (role.getName().equals("admin") || role.getName().equals("logistique") || role.getName().equals("consulteur")) {
	                listePlans = getAllPlans();
	            } else {
	                for (Ateliers atelier : login.getAteliers()) {
	                    at.add(atelier);
	                }
	                listePlans = planRepository.findAllByAtelierInOrderByIdDesc(at);
	            }
	        }
	        return listePlans;
	    }









	
	
	
	
	
	
	
	
	
    //	afficher historique de plan avec methode log4j
//	  public List<HistoriqueDTO> getHistoriqueByPlanId(Long planId) {
//	        logger.info("Fetching history for plan ID: " + planId);
//	        List<Historique> historiques = historiqueRepository.findByNumeroPlanIdOrderByDate(planId);
//	        List<HistoriqueDTO> historiqueDTOs = new ArrayList<>();
//
//	        for (Historique historique : historiques) {
//	            HistoriqueDTO historiqueDTO = new HistoriqueDTO();
//	            historiqueDTO.setId(historique.getId());
//	            historiqueDTO.setDate(historique.getDate());
//	            historiqueDTO.setTypeHistorique(historique.getType().toString());
//	            historiqueDTO.setStatutPlan(historique.getNumeroPlan().getStatut().toString());
//	            historiqueDTO.setEvenement(getEvenement(historique.getType()));
//	            historiqueDTOs.add(historiqueDTO);
//
//	            logger.info("Processed historique: " + historiqueDTO.toString());
//	        }
//
//	        return historiqueDTOs;
//	    }
//
//	    private String getEvenement(TypeHistorique type) {
//	        switch (type) {
//	            case Création:
//	                return "Création de plan";
//	            case Modification:
//	                return "Modification de plan";
//	            case Validation:
//	                return "Validation de plan";
//	            default:
//	                return "Type d'historique inconnu";
//	        }
//	    }
       // fin	afficher historique de plan avec methode log4j
	
	


    
	
	
	
	

    
    
    
    
//    @Override
//    public Page<Plan> PlanByProject(Pageable pageable,Optional<Projet> affaire) {
//        int pageSize = pageable.getPageSize();
//        int currentPage = pageable.getPageNumber();
//        int startItem = currentPage * pageSize;
//        List<Plan> listCons;
//        List<Plan> listePlan = planRepository.findAllByProjet(affaire);
//
//        if (listePlan.size() < startItem) {
//            listCons = Collections.emptyList();
//        } else {
//            int toIndex = Math.min(startItem + pageSize, listePlan.size());
//            listCons = listePlan.subList(startItem, toIndex);
//        }
//        Page<Plan> consultPage = new PageImpl<Plan>(listCons, PageRequest.of(currentPage, pageSize), listePlan.size());
//
//        return consultPage;
//    }
//
//    @Override
//    public Page<Plan> SearchplanByProjectAndAtelier(Pageable pageable, Login login, long idaffaire, long idPlan, long atelier) throws ParseException {
//        int pageSize = pageable.getPageSize();
//        int currentPage = pageable.getPageNumber();
//        int startItem = currentPage * pageSize;
//        List<Plan> listCons;
//        List<Plan> listePlan = planSearchDoa.searchPlanByProjet(login, idaffaire, idPlan, atelier);
//
//        if (listePlan.size() < startItem) {
//            listCons = Collections.emptyList();
//        } else {
//            int toIndex = Math.min(startItem + pageSize, listePlan.size());
//            listCons = listePlan.subList(startItem, toIndex);
//        }
//        Page<Plan> consultPage = new PageImpl<Plan>(listCons, PageRequest.of(currentPage, pageSize), listePlan.size());
//
//        return consultPage;
//    }
//
//    @Override
//    public Page<Plan> PlanByProjectAndAtelier(Pageable pageable, Optional<Projet> affaire, Ateliers atelier) {
//        int pageSize = pageable.getPageSize();
//        int currentPage = pageable.getPageNumber();
//        int startItem = currentPage * pageSize;
//        List<Plan> listCons;
//        List<Plan> listePlan = planRepository.findAllByProjetAndAtelierOrderByIdAsc(affaire, atelier);
//
//        if (listePlan.size() < startItem) {
//            listCons = Collections.emptyList();
//        } else {
//            int toIndex = Math.min(startItem + pageSize, listePlan.size());
//            listCons = listePlan.subList(startItem, toIndex);
//        }
//        Page<Plan> consultPage = new PageImpl<Plan>(listCons, PageRequest.of(currentPage, pageSize), listePlan.size());
//
//        return consultPage;
//    }
	
    
    
    
    
    

 
    
}
























//    @Override
//	public Plan editPlan(Plan plan, long id) throws IOException,PlanNotFoundException,EmptyResultDataAccessException{
//		if (planRepository.existsById(id)) 
//		{
//			
//			Optional<Plan> optionalplanOld = planRepository.findById(id);
//			Plan planOld = optionalplanOld.get();
//			planOld.setArticle(plan.getArticle());
//			planOld.setNiveau(plan.getNiveau());
//			planOld.setEmplacement(plan.getEmplacement());
//			planOld.setAffaire(plan.getAffaire());
//			planOld.setAtelier(plan.getAtelier());
//			return planRepository.save(planOld);
//		}
//		else
//			return null;
//	}
//	@Override
//	public Plan updatePlan(Plan plan, long id) throws IOException,PlanNotFoundException,EmptyResultDataAccessException{
//
//            plan.setId(id);
//			return planRepository.save(plan);
//	}
    

    
//    public void update(Plan plan) {
//        planRepository.save(plan);
//    }

//    @Override
//    public Plan editPlan(Plan newPlan, Long id) {
//        Plan existingPlan = planRepository.findById(id).orElse(null);
//        if (existingPlan != null) {
//            if (newPlan.getAtelier() != null) {
//                existingPlan.setAtelier(newPlan.getAtelier());
//            }
//            if (newPlan.getArticle() != null) {
//                existingPlan.setArticle(newPlan.getArticle());
//            }
//            if (newPlan.getAffaire() != null) {
//                existingPlan.setAffaire(newPlan.getAffaire());
//            }
//            if (newPlan.getNiveau() != null) {
//                existingPlan.setNiveau(newPlan.getNiveau());
//            }
//            if (newPlan.getEmplacement() != null) {
//                existingPlan.setEmplacement(newPlan.getEmplacement());
//            }
//            if (newPlan.getPieceJointe() != null) {
//                existingPlan.setPieceJointe(newPlan.getPieceJointe());
//            }
//            if (newPlan.getDessinePar() != null) {
//                existingPlan.setDessinePar(newPlan.getDessinePar());
//            }
//            if (newPlan.getControlPar() != null) {
//            	 existingPlan.setControlPar(newPlan.getControlPar());
//            } 
//            StatutPlan currentStatut = existingPlan.getStatut();
//            if (currentStatut.equals(StatutPlan.VALIDÉ)) {
//                existingPlan.setStatut(StatutPlan.EN_COURS);
//            } else if (currentStatut.equals(StatutPlan.BROUILLON)) {
//                existingPlan.setStatut(StatutPlan.EN_COURS);
//            }
//
//            return planRepository.save(existingPlan);
//        }
//        return null;
//    }