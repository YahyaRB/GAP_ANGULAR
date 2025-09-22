package ma.gap.controller;

import ma.gap.dtos.NomenclatureDTO;
import ma.gap.entity.DetailLivraison;
import ma.gap.entity.Nomenclature;
import ma.gap.service.NomenclatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nomenclatures")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NomenclatureController {

    @Autowired
    private NomenclatureService nomenclatureService;

    @PostMapping("/save/{ofId}")
    public ResponseEntity<String> ajouterDetail(@RequestBody Nomenclature nomenclature,@PathVariable Long ofId) {
        try {System.out.println(nomenclature);

            nomenclatureService.save(nomenclature,ofId);
            return new ResponseEntity<>("Nomenclature ajouté avec succès", HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("=== ERREUR DANS LE CONTRÔLEUR ===");
            e.printStackTrace();
            return new ResponseEntity<>("Erreur lors de l'ajout du nomenclature: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Récupère toutes les nomenclatures d'un ordre de fabrication spécifique
     * @param ofId ID de l'ordre de fabrication
     * @return Liste des nomenclatures
     */
    @GetMapping("/by-of/{ofId}")
    public ResponseEntity<List<NomenclatureDTO>> getNomenclaturesByOF(@PathVariable Long ofId) {
        try {
            List<NomenclatureDTO> nomenclatures = nomenclatureService.getNomenclaturesByOF(ofId);

            if (nomenclatures.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(nomenclatures);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des nomenclatures pour OF " + ofId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une nomenclature par son ID
     * @param id ID de la nomenclature
     * @return La nomenclature trouvée
     */
    @GetMapping("/{id}")
    public ResponseEntity<NomenclatureDTO> getNomenclatureById(@PathVariable Long id) {
        try {
            NomenclatureDTO nomenclature = nomenclatureService.getNomenclatureById(id);

            if (nomenclature == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(nomenclature);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la nomenclature " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


