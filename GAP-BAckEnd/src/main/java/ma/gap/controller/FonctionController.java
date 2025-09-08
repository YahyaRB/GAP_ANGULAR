package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.Fonction;
import ma.gap.service.FonctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/fonction")
@AllArgsConstructor
public class FonctionController {

    private final FonctionService fonctionService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Fonction>> getAllFonctions() {
        try {
            List<Fonction> fonctions = fonctionService.findAllFonctions();
            return ResponseEntity.ok(fonctions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Fonction> getFonctionById(@PathVariable long id) {
        try {
            Optional<Fonction> fonction = fonctionService.findFonctionById(id);
            return fonction.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addFonction(@RequestBody Fonction fonction) {
        try {
            fonctionService.saveFonction(fonction);
            return ResponseEntity.status(HttpStatus.CREATED).body("Fonction ajoutée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de l'ajout de la fonction.");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateFonction(@PathVariable long id, @RequestBody Fonction fonction) {
        try {
            Optional<Fonction> existingFonction = fonctionService.findFonctionById(id);
            if (!existingFonction.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Fonction non trouvée. Mise à jour impossible.");
            }
            fonctionService.updateFonction(fonction, id);
            return ResponseEntity.ok("Fonction mise à jour avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de la mise à jour.");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFonction(@PathVariable long id) {
        try {
            fonctionService.deleteFonction(id);
            return ResponseEntity.ok("Fonction supprimée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de la suppression.");
        }
    }
}
