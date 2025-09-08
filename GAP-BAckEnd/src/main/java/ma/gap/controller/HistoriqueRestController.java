//package ma.gap.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//import ma.gap.entity.Historique;
//import ma.gap.repository.PlanImpService;
//import ma.gap.repository.PlanService;
//import ma.gap.dtos.HistoriqueDTO;
//import ma.gap.exceptions.PlanNotFoundException;
//
//import java.util.List;
//@RestController
//public class HistoriqueRestController {
//
//    @Autowired
//    private PlanImpService planImpService;
//    @Autowired
//    private PlanService planService;
//    
    
    
//    @GetMapping("/api/plan/{planId}/historique")
//    public List<HistoriqueDTO> getHistoriqueByPlanId(@PathVariable Long planId) {
//        return planImpService.getHistoriqueByPlanId(planId);
//    }
    
    
//    @GetMapping("/plans/{planId}")
//    public ResponseEntity<List<Historique>> getPlanHistorique(@PathVariable Long planId) {
//        try {
//            List<Historique> historiques = planService.getPlanHistorique(planId);
//            return ResponseEntity.ok(historiques);
//        } catch (PlanNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
    
    
  // } 
