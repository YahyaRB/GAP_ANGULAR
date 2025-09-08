package ma.gap.controller;


import ma.gap.entity.Historique;
import ma.gap.service.HistoriqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/")

public class HistoriqueController {

    private HistoriqueService historiqueService;


    @Autowired
    public HistoriqueController(HistoriqueService historiqueService) {this.historiqueService = historiqueService;}

    @PostMapping(value = "/create")
    public String createHistorique(@RequestBody Historique historique) {
        historique =historiqueService.createHistorique(historique);
        return "redirect:/getall";
    }

    @PutMapping(value = "/update/{id}")
    public String updateHistorique(@PathVariable("id") long id  , @RequestBody Historique historique) {
        Historique updateHistorique = historiqueService.updateHistorique(historique, id);
        return "redirect:/getall";
    }

    @GetMapping("/get/{id}")
    public Optional<Historique> getHistorique(@PathVariable("id") long id) {
        Optional<Historique> historique = historiqueService.findHistoriqueById(id);
        return historique;
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Historique>> getAllHistorique() {
        List<Historique> planList = historiqueService.getAllHistorique();
        return new ResponseEntity<>(planList, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public void deleteHistorique(@PathVariable("id") long id) {
        historiqueService.deleteHistorique(id);
    }
}
