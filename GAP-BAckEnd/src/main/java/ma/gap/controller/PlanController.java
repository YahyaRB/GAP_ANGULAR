package ma.gap.controller;

import ma.gap.service.*;
import org.springframework.dao.DataIntegrityViolationException;

import ma.gap.config.GlobalVariableConfig;
import ma.gap.entity.*;

import ma.gap.dtos.EmployeeDTO;
import ma.gap.dtos.HistoriqueDTO;
import ma.gap.enums.StatutPlan;
import ma.gap.enums.TypeNomenclature;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import ma.gap.exceptions.PlanNotFoundException;
import net.sf.jasperreports.engine.JRException;
import ma.gap.repository.ArticleRepository;
import ma.gap.repository.AtelierRepository;
import ma.gap.repository.HistoriqueRepository;
import ma.gap.repository.PlanRepository;
import ma.gap.repository.PlanSearchDoa;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/plan")
@AllArgsConstructor

public class PlanController {
//	 private static final Logger logger = Logger.getLogger(PlanController.class);
	private PlanImpService planImpService;
	private PlanRepository planRepository;
	private PlanService planService;
	private UserImpService userImpService;
    private final GlobalVariableConfig globalVariableConfig;
    private ProjetService projetService;
    private AtelierService atelierService;
	private ArticleService articleService;
	private ArticleAchService articleAchService;
	private AtelierService atelierImpService;
	private ArticleImpService articleImpService;
	private PlanSearchDoa planSearchDao; 
    private HistoriqueRepository historiqueRepository;
	
	
// create Plan	
    @PostMapping("/plan/create")
    @PreAuthorize("hasAnyAuthority('admin')")
    public String createPlan(@ModelAttribute Plan plan) throws IOException {
        planService.createPlan(plan);
        return "redirect:/plan/getall";
    }

    @GetMapping("/plan/{planId}/start")
    @PreAuthorize("hasAnyAuthority('admin','consulteur')")
    public String showDeposerPlanForm(@PathVariable Long planId, Model model) throws PlanNotFoundException {
        List<User> logins = userImpService.getAllLogins();

        Plan plan = planService.findPlanById(planId);
        Historique lastHistorique = historiqueRepository.findFirstByNumeroPlanOrderByDateDesc(plan);
        String lastIndice = (lastHistorique != null) ? lastHistorique.getIndice() : "None";
        model.addAttribute("lastIndice", lastIndice);

        return "start-plan-form";
    }

    @PostMapping("/plan/{planId}/start")
    @PreAuthorize("hasAnyAuthority('admin')")
    public String deposerPlan(@PathVariable Long planId, @RequestParam("pieceJointe") MultipartFile file, @RequestParam("dessinePar") User dessineParName, @RequestParam("indice") String indice) throws PlanNotFoundException {
        Plan plan = planService.deposerPlan(planId, file, dessineParName, indice);
        return "redirect:/plan/getall";
    }
    
    
    
    
    
 // telecharger PieceJointe   
    @GetMapping("/plan/{planId}/download")
    @PreAuthorize("hasAnyAuthority('admin','consulteur')")
    public ResponseEntity<byte[]> telechargerPieceJointe(@PathVariable Long planId) {
        try {
            return planService.telechargerPieceJointe(planId);
        } catch (PlanNotFoundException | FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
// fin télécharger PieceJointe    
    
    
    
    
    
    
// validation Plan    
    @PostMapping("/plan/{planId}/validate")
    @PreAuthorize("hasAnyAuthority('admin')")
    public String validatePlan(@PathVariable Long planId, @RequestParam("controlPar") User controlPar) throws PlanNotFoundException {
        Plan plan = planService.validatePlan(planId,controlPar);
        return "redirect:/plan/getall";
    }
// fin validation plan 
    
    
    
    
    
    
    
    
// editer plan
    @PostMapping(value = "/plan/update/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public String editPlan(@PathVariable("id") long id, Plan updatedPlan, RedirectAttributes attributes) throws IOException {
        try {
            Plan existingPlan = planService.findPlanById(id).orElseThrow(() -> new PlanNotFoundException("Plan not found"));
            if (existingPlan.getStatut() == StatutPlan.LANCER) {
                attributes.addFlashAttribute("error", "Cannot edit a launched plan");
                return "redirect:/plan/getall";
            }

            if (updatedPlan.getArticle() != null) {
                existingPlan.setArticle(updatedPlan.getArticle());
            }
            if (updatedPlan.getAffaire() != null) {
                existingPlan.setAffaire(updatedPlan.getAffaire());
            }
            if (updatedPlan.getAtelier() != null) {
                existingPlan.setAtelier(updatedPlan.getAtelier());
            }
            if (updatedPlan.getNiveau() != null) {
                existingPlan.setNiveau(updatedPlan.getNiveau());
            }
            if (updatedPlan.getEmplacement() != null) {
                existingPlan.setEmplacement(updatedPlan.getEmplacement());
            }
            if (updatedPlan.getDatePlan() != null) {
                existingPlan.setDatePlan(updatedPlan.getDatePlan());
            }
            if (updatedPlan.getPieceJointe() != null) {
                existingPlan.setPieceJointe(updatedPlan.getPieceJointe());
            }
            if (updatedPlan.getDessinePar() != null) {
                existingPlan.setDessinePar(updatedPlan.getDessinePar());
            }
            if (updatedPlan.getControlPar() != null) {
                existingPlan.setControlPar(updatedPlan.getControlPar());
            }

            if (existingPlan.getStatut() == StatutPlan.VALIDÉ) {
                existingPlan.setStatut(StatutPlan.EN_COURS);
            } else if (existingPlan.getStatut() == StatutPlan.BROUILLON) {
                existingPlan.setStatut(StatutPlan.BROUILLON);
            }

            planService.editPlan(existingPlan, id);
        } catch (PlanNotFoundException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/plan/getall";
        }
        return "redirect:/plan/getall";
    }
// fin editer plan    

    
    
    
    
    
    
// afficher le plan par id 
    @GetMapping("/plan/get/{id}")
    @PreAuthorize("hasAnyAuthority('admin','consulteur')")
    public ModelAndView getPlanById(@PathVariable("id") long id) {
        ModelAndView modelAndView = new ModelAndView();
        
        Optional<Plan> plan = planService.findPlanById(id);
        plan.ifPresent(p -> {
            modelAndView.addObject("plan", p);
            modelAndView.setViewName("templates/Plan/plan.html");
        });
        return modelAndView;
    }
 // fin afficher le plan par id     
    
 
    
    
    
    

    @GetMapping(value = "/getall/{idUser}")
    @PreAuthorize("hasAnyAuthority('admin','consulteur')")
    public List<Plan> getAllPlans(@PathVariable long idUser) {
        User user = userImpService.findbyusername(idUser);
        List<Plan> plans = new ArrayList<>();
        for(Role role:user.getRoles()){
        if (role.getName().equals("admin") || role.getName().equals("logistique") || role.getName().equals("consulteur") ) {
            plans = planRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        } else {
            plans = planService.getAllPlans(); 
            }
        }




        return plans;
    }

    

    @GetMapping(value = "/plan/Supprimer/{id}")
    public String deletePlan(@PathVariable("id") long id, RedirectAttributes attributes) {
        try {
            Plan plan = planService.findPlanById(id).orElseThrow(() -> new PlanNotFoundException("Plan not found"));
            if (plan.getStatut() == StatutPlan.LANCER) {
                attributes.addFlashAttribute("errorDelete", "Le plan a déjà été lancé et ne peut pas être supprimé.");
                return "redirect:/plan/getall";
            }
            if (plan.getStatut() == StatutPlan.EN_COURS) {
                attributes.addFlashAttribute("errorDelete", "Le plan est en cours et ne peut pas être supprimé.");
                return "redirect:/plan/getall";
            }
            planService.deletePlanAndAssociatedHistoriques(id);
            attributes.addFlashAttribute("message", "Le plan a été supprimé avec succès");
        } catch (PlanNotFoundException e) {
            attributes.addFlashAttribute("errorDelete", "Le Plan que vous essayez de supprimer n'existe pas");
        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("errorDelete", "Impossible de supprimer le plan en raison de contraintes d'intégrité des données");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorDelete", "Une erreur s'est produite lors de la suppression du plan");
        }
        return "redirect:/plan/getall";
    }
    
// fin delete plan
	
	
	


// filtrer l'article par Atelier   
    @PostMapping("/articles")
    @PreAuthorize("hasAnyAuthority('admin')")
    public String getArticlesByAtelier(@RequestParam("atelierId") Long atelierId, Model model) {
        Optional<Article> articles = planService.getArticlesByAtelier(atelierId);
        model.addAttribute("articles", articles);
        return "Projet/ListArticle";
    }
// fin filtrer l'article par Atelier    
    

    @GetMapping("/plan/{planId}/historique")
    @PreAuthorize("hasAnyAuthority('admin','consulteur')")
    @ResponseBody
    public List<HistoriqueDTO> getHistorique(@PathVariable Long planId) {
        return planImpService.getHistoriqueByPlanId(planId);
    }   
    
    


    @GetMapping("plan/Search")
    public List<Plan> SearchPlan(@RequestParam("idUser") long idUser,@RequestParam("idprojet") long idProjet, @RequestParam("idatelier") long idAtelier,
                             @RequestParam("idarticle") long idArticle, @RequestParam("statutPlan") Optional<String> statutPlan,
                             @RequestParam("idPlan") Optional<Long> idPlan) 
                             throws ParseException, PlanNotFoundException, IOException {

        List<Plan> listePlanSearch = planSearchDao.searchPlan(idUser,idProjet, idAtelier, idArticle, statutPlan.orElse(""), idPlan.orElse(0L));
        Collections.reverse(listePlanSearch);

        return listePlanSearch;
    }
    
    

	
}
