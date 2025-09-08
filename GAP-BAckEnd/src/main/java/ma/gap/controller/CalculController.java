package ma.gap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;
import ma.gap.entity.Ateliers;
import ma.gap.entity.CalculPerProjet;
import ma.gap.service.AtelierImpService;
import ma.gap.service.CalculImpService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/")

@AllArgsConstructor
public class CalculController {

    private CalculImpService calculImpService;

    private AtelierImpService atelierImpService;




    @GetMapping("/Calcul/CalculerParProjet")
    public String doCalculPerProject(@RequestParam(name = "idUser") long idUser,@RequestParam(name = "atelier") Long id, @RequestParam(name = "month") Integer month, @RequestParam(name = "year") Integer year,Model model) throws JsonProcessingException, ParseException {
        Ateliers atelier = atelierImpService.getAtelierById(id);
        List<CalculPerProjet> list = calculImpService.getAffectationPerProject(id,month,year);
        if (list.isEmpty()){
            model.addAttribute("Affectations",null);
            model.addAttribute("donneesNull","Aucun Projet était affecter à cet atelier au cours du mois : "+month+" ( "+atelier.getDesignation()+" )");
            model.addAttribute("Ateliers",atelierImpService.allAteliers(idUser));
        }else

        model.addAttribute("Affectations",list);
        model.addAttribute("Ateliers",atelierImpService.allAteliers(idUser));

        return "Calcul/ListCalcul";
    }
}
