package ma.gap.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import javax.persistence.EntityNotFoundException;

import ma.gap.repository.PlanRepository;
import ma.gap.service.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import ma.gap.entity.ArticleAch;
import ma.gap.entity.CombinedView;
import ma.gap.entity.Nomenclature;
import ma.gap.entity.NomenclatureArticleAch;
import ma.gap.entity.Plan;
import ma.gap.repository.ArticleAchRepository;
import ma.gap.repository.NomenclatureSearchDao;
import ma.gap.enums.TypeNomenclature;
import ma.gap.exceptions.NomenclatureNotFoundException;
import ma.gap.exceptions.PlanNotFoundException;
import net.sf.jasperreports.engine.JRException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/")
@AllArgsConstructor
public class NomenclatureController {

    private NomenclatureService nomenclatureService;
    private PlanService planService;
    private ArticleAchService articleAchService;
    private ArticleAchRepository articleAchRepository;
    private NomenclatureArticleAchService nomenclatureArticleAchService;
    private NomenclatureSearchDao nomenclatureSearchDao ;
    private NomenclatureImpService nomenclatureImpService;
    private ArticleAchImpService articleAchImpService;
    private PlanRepository planRepository;

    @GetMapping("/nomenclature/getall")
    public String listNomenclatures(Model model) {
        List<Nomenclature> nomenclatures = nomenclatureService.getAllNomenclatures();
        List<NomenclatureArticleAch> nomenclatureArticleAchs = nomenclatureArticleAchService.getAllNomenclatureArticleAchs();
        List<ArticleAch> articleAchs = articleAchService.getAllArticleAchs();
        List<Plan> plans = planService.getAllPlans();

        List<CombinedView> combinedViews = new ArrayList<>();

        for (Nomenclature nomenclature : nomenclatures) {
            for (NomenclatureArticleAch articleAchRel : nomenclatureArticleAchs) {
                if (articleAchRel.getNomenclature().equals(nomenclature)) {
                    CombinedView combinedView = new CombinedView();
                    combinedView.setNomenclature(nomenclature);
                    combinedView.setNomenclatureArticleAch(articleAchRel);
                    combinedView.setArticleAch(articleAchRel.getArticleAch());
                    Plan associatedPlan = plans.stream()
                            .filter(plan -> plan.getId().equals(nomenclature.getNumeroPlan()))
                            .findFirst()
                            .orElse(null);
                    combinedView.setPlan(associatedPlan);

                    combinedViews.add(combinedView);
                }
            }
        }
        model.addAttribute("combinedViews", combinedViews);
        model.addAttribute("plans", planService.getAllPlans());
        model.addAttribute("typeNomenclatures", TypeNomenclature.values());
        model.addAttribute("nomenclatures", nomenclatureService.getAllNomenclatures());
        model.addAttribute("articleAchs", articleAchService.getAllArticleAchs());
        return "Nomenclature/nomenclature";
    }

    @GetMapping("/nomenclature/new")
    public String showCreateNomenclatureForm(Model model) {
        model.addAttribute("nomenclature", new Nomenclature());
        model.addAttribute("articleAchList", articleAchRepository.findAll());
        model.addAttribute("plans", planService.getAllPlans());
        model.addAttribute("typeNomenclatures", TypeNomenclature.values());
        return "Nomenclature/nomenclature";
    }

    @PostMapping("/nomenclature/create")
    public String createNomenclature(
            @ModelAttribute Nomenclature nomenclature,
            @RequestParam(value = "articleAchId") List<Long> articleAchIds,
            @RequestParam(value = "largeur") List<Float> largeurs,
            @RequestParam(value = "longueur") List<Float> longueurs,
            @RequestParam(value = "epaisseur") List<Float> epaisseurs,
            @RequestParam(value = "finition") List<String> finitions,
            @RequestParam(value = "quantite") List<Float> quantites) {

        if (articleAchIds == null || articleAchIds.isEmpty()) {
            return "error";
        }

        List<NomenclatureArticleAch> articleAchList = new ArrayList<>();
        for (int i = 0; i < articleAchIds.size(); i++) {
            NomenclatureArticleAch articleAch = new NomenclatureArticleAch();
            articleAch.setArticleAch(articleAchRepository.findById(articleAchIds.get(i)).orElse(null));
            articleAch.setFinition(finitions.get(i));
            articleAch.setQuantite(quantites.get(i));

            if (nomenclature.getType().equals(TypeNomenclature.Quincaillerie)) {
                articleAch.setLargeur(null);
                articleAch.setLongueur(null);
                articleAch.setEpaisseur(null);
            } else {
                articleAch.setLargeur(largeurs.get(i));
                articleAch.setLongueur(longueurs.get(i));
                articleAch.setEpaisseur(epaisseurs.get(i));
            }

            articleAchList.add(articleAch);
        }
        nomenclatureService.createNomenclature(nomenclature, articleAchList);
        return "redirect:/nomenclature/getall";
    }





    @PostMapping("/nomenclature/create/{planId}")
    public String createNomenclature(
            @PathVariable Long planId,
            @ModelAttribute Nomenclature nomenclature,
            @RequestParam(value = "articleAchId") List<Long> articleAchIds,
            @RequestParam(value = "largeur", required = false) List<Float> largeurs,
            @RequestParam(value = "longueur", required = false) List<Float> longueurs,
            @RequestParam(value = "epaisseur", required = false) List<Float> epaisseurs,
            @RequestParam(value = "finition") List<String> finitions,
            @RequestParam(value = "quantite") List<Float> quantites) {

        if (articleAchIds == null || articleAchIds.isEmpty()) {
            return "error";
        }

        Plan plan = planRepository.findById(planId).orElse(null);
        if (plan == null) {
            return "error";
        }

        nomenclature.setNumeroPlan(plan);

        List<NomenclatureArticleAch> articleAchList = new ArrayList<>();
        for (int i = 0; i < articleAchIds.size(); i++) {
            NomenclatureArticleAch articleAch = new NomenclatureArticleAch();
            articleAch.setArticleAch(articleAchRepository.findById(articleAchIds.get(i)).orElse(null));
            articleAch.setFinition(finitions.get(i));
            articleAch.setQuantite(quantites.get(i));

            if (nomenclature.getType().equals(TypeNomenclature.Quincaillerie)) {
                articleAch.setLargeur(null);
                articleAch.setLongueur(null);
                articleAch.setEpaisseur(null);
            } else {
                articleAch.setLargeur(largeurs != null ? largeurs.get(i) : null);
                articleAch.setLongueur(longueurs != null ? longueurs.get(i) : null);
                articleAch.setEpaisseur(epaisseurs != null ? epaisseurs.get(i) : null);
            }

            articleAchList.add(articleAch);
        }
        nomenclatureService.createNomenclature(nomenclature, articleAchList);
        return "redirect:/nomenclature/getall";
    }









    @GetMapping("/nomenclature/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<Nomenclature> nomenclature = nomenclatureService.findById(id);
        List<ArticleAch> articleAchList = articleAchRepository.findAll();

        model.addAttribute("nomenclature", nomenclature);
        model.addAttribute("articleAchList", articleAchList);
        model.addAttribute("typeNomenclatures", TypeNomenclature.values());

        return "Nomenclature/nomenclature";
    }



    @PostMapping("/nomenclature/update/{id}")
    public String updateNomenclature(Nomenclature nomenclature, NomenclatureArticleAch nomenclatureArticleAch, @PathVariable("id") Long id) {

        nomenclatureArticleAch.setNomenclature(nomenclature);

        if ("Quincaillerie".equalsIgnoreCase(nomenclature.getType().toString())) {
            nomenclatureArticleAch.setLongueur(null);
            nomenclatureArticleAch.setLargeur(null);
            nomenclatureArticleAch.setEpaisseur(null);
        }

        nomenclatureArticleAchService.updateNomenclature(nomenclatureArticleAch, id);
        nomenclatureService.updateNomenclature(nomenclature, nomenclatureArticleAch.getNomenclature().getId());

        return "redirect:/nomenclature/getall";
    }






    @PostMapping("/nomenclature/Supprimer/{id}")
    public String deleteNomenclature(@PathVariable("id") Long id, RedirectAttributes attributes) {
        try {
            nomenclatureService.deleteNomenclature(id);
            attributes.addFlashAttribute("success", "Nomenclature supprimée avec succès.");
        } catch (EntityNotFoundException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erreur lors de la suppression de la nomenclature.");
        }
        return "redirect:/nomenclature/getall";
    }


    @GetMapping("/nomenclature/reports/{id}")
    public ResponseEntity<byte[]> generateDemande(@PathVariable("id") Long id) throws JRException, IOException {

        ResponseEntity<byte[]> NomenclatureEtat = null;
        try {
            NomenclatureEtat = nomenclatureService.generateReport(id);
        } catch (FileNotFoundException | EmptyResultDataAccessException | NomenclatureNotFoundException e) {

            e.printStackTrace();
        }
        return NomenclatureEtat;
    }


    @GetMapping("/nomenclature/Search")
    public List<Nomenclature> searchNomenclature(Model model,
                                     @RequestParam(value = "idArticleAch", required = false) Long idArticleAch,
                                     @RequestParam(value = "idPlan", required = false) Long idPlan,
                                     @RequestParam(value = "idNomenclature", required = false) Long idNomenclature)
            throws IOException, ParseException {

        List<Nomenclature> listeNomenclatureSearch = nomenclatureSearchDao.searchNomenclature(idArticleAch, idPlan, idNomenclature);
        Collections.reverse(listeNomenclatureSearch);

        return listeNomenclatureSearch;
    }


}

    
    


