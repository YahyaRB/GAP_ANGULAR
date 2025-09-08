package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.Chauffeur;
import ma.gap.repository.ChauffeurRepository;
import ma.gap.repository.ChauffeurSearchDao;
import ma.gap.service.ChauffeurService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chauffeur")
@AllArgsConstructor
public class ChauffeursController {

    private ChauffeurService chauffeurService;
    private ChauffeurSearchDao chauffeurSearchDao;
    private ChauffeurRepository chauffeurRepository;

    @GetMapping("/getAll")
    public ResponseEntity<List<Chauffeur>> findAllChauffeurs() {
        try {
            List<Chauffeur> chauffeurs = chauffeurService.allChauffeurs();
            return ResponseEntity.ok(chauffeurs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/searchById/{id}")
    public ResponseEntity<Chauffeur> searchChauffeurById(@PathVariable long id) {
        try {
            Optional<Chauffeur> chauffeur = chauffeurService.chauffeurById(id);
            return chauffeur.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> saveChauffeur(@RequestBody Chauffeur chauffeur) {
        try {
            chauffeurService.saveChauffeur(chauffeur);
            return ResponseEntity.status(HttpStatus.CREATED).body("Chauffeur enregistré avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue est survenue.");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> editChauffeur(@PathVariable long id, @RequestBody Chauffeur chauffeur) {
        try {
            Optional<Chauffeur> existingChauffeur = chauffeurService.chauffeurById(id);
            if (!existingChauffeur.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Chauffeur non trouvé. Édition impossible.");
            }
            chauffeurService.updateChauffeur(chauffeur, id);
            return ResponseEntity.ok("Chauffeur édité avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue est survenue.");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteChauffeur(@PathVariable long id) {
        try {
            chauffeurService.deleteChauffeur(id);
            return ResponseEntity.ok("Chauffeur supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping("/existeByNomComplet")
    public ResponseEntity<Boolean> existeByNomComplet(
            @RequestParam String nom,
            @RequestParam String prenom) {
        try {
            boolean exists = chauffeurService.existeByNomComplet(nom, prenom);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Chauffeur>> searchChauffeurs(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String matricule) {
        try {
            List<Chauffeur> chauffeurs = chauffeurSearchDao.searchChauffeur(nom, prenom, matricule);
            return ResponseEntity.ok(chauffeurs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/existeByMatricule/{matricule}")
    public ResponseEntity<Boolean> existeByMatricule(@PathVariable int matricule) {
        try {
            boolean exists = chauffeurService.existeByMatricule(matricule);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/hasDeliveries/{idChauffeur}")
    public ResponseEntity<Boolean> hasDeliveries(@PathVariable long idChauffeur) {
        try {
            boolean hasDeliveries = chauffeurService.hasDeliveries(idChauffeur);
            return ResponseEntity.ok(hasDeliveries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('admin','EXPORT')")
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String matricule) throws IOException, ParseException {

        List<Chauffeur> chauffeurs = chauffeurSearchDao.searchChauffeur(nom, prenom, matricule);

        // Créer un classeur Excel
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Chauffeurs");

        // Style des en-têtes
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);

        // En-têtes de colonnes
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Id", "Matricule", "Nom", "Prénom"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }

        // Remplir les données
        int rowNum = 1;
        for (Chauffeur chauffeur : chauffeurs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(chauffeur.getId());
            row.createCell(1).setCellValue(chauffeur.getMatricule());
            row.createCell(2).setCellValue(chauffeur.getNom());
            row.createCell(3).setCellValue(chauffeur.getPrenom());
        }

        // Configurer la réponse HTTP
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Chauffeurs.xls");

        // Écrire dans la réponse
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
