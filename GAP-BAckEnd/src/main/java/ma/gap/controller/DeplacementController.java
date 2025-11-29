package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.message.ResponseMessage;
import ma.gap.repository.MotifDepRepository;
import ma.gap.repository.ProjetRepository;
import ma.gap.exceptions.OrdreMissionNotFoundException;
import ma.gap.service.DeplacementImpService;
import ma.gap.service.EmployeeImpService;
import ma.gap.service.FilesStorageService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dpl")
@AllArgsConstructor
public class DeplacementController {

    private FilesStorageService storageService;
    private DeplacementImpService deplacementImpService;

    @GetMapping(value = "/getAll/{idUser}")
    public ResponseEntity<Page<Deplacement>> findAll(@PathVariable("idUser") long idUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<Deplacement> listeDeplacements = deplacementImpService.allDeplacement(idUser, pageable);
            return ResponseEntity.ok(listeDeplacements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/add")
    public ResponseEntity<?> save(@RequestBody Deplacement deplacement) {
        try {
            Deplacement savedDeplacement = deplacementImpService.saveDeplacement(deplacement);
            // Récupérer l'ID du déplacement enregistré
            Long id = savedDeplacement.getId(); // Si ton entité OrdreFabrication a un attribut 'id'

            return ResponseEntity.status(HttpStatus.CREATED).body(id); // Retourner l'ID dans la réponse
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @PutMapping(value = "/update/{id}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<?> edit(@PathVariable("id") long id, @RequestBody Deplacement deplacement) {
        try {
            Optional<Deplacement> optionalArticle = Optional.ofNullable(deplacementImpService.getById(id));

            if (!optionalArticle.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Déplacement non trouvée. Édition impossible.");
            }

            Deplacement updatedDeplacement = deplacementImpService.editDeplacement(deplacement, id);
            Long idDep = updatedDeplacement.getId(); // Si ton entité OrdreFabrication a un attribut 'id'

            return ResponseEntity.status(HttpStatus.CREATED).body(idDep); // Retourner l'ID dans la réponse

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<String> deleteLivraison(@PathVariable("id") long id) {
        try {
            deplacementImpService.deleteDeplacement(id);
            return ResponseEntity.ok("Déplacement supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping("/search")
    public Page<Deplacement> SearchDeplacements(@RequestParam("idUser") long idUser,
            @RequestParam("idemploye") long idemploye, @RequestParam("idprojet") long idprojet,
            @RequestParam("idatelier") long idatelier, @RequestParam("motif") String motif,
            @RequestParam("dateDebut") String dateDebut, @RequestParam("dateFin") String dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws ParseException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return deplacementImpService.searchDeplacement(idUser, idemploye, idprojet, idatelier,
                motif, dateDebut, dateFin, pageable);
    }

    // *** MÉTHODE AMÉLIORÉE POUR L'IMPRESSION ***
    @GetMapping("/Deplacement/Imprimer/{id}")
    @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<?> generateDemande(@PathVariable("id") Long id) {
        try {
            // Vérifier que l'ID existe
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("ID de déplacement invalide : " + id);
            }

            // Vérifier que le déplacement existe
            Deplacement deplacement = deplacementImpService.getById(id);
            if (deplacement == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Déplacement non trouvé avec l'ID : " + id);
            }

            // Vérifier que le déplacement a au moins un employé
            if (deplacement.getEmployee() == null || deplacement.getEmployee().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Aucun employé associé au déplacement ID : " + id);
            }

            ResponseEntity<byte[]> OmEtat = deplacementImpService.generateOm(id);
            return OmEtat;

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Fichier template JasperReports non trouvé. Vérifiez le fichier OrdreMission.jrxml dans resources/files/");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Déplacement non trouvé avec l'ID : " + id);
        } catch (OrdreMissionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ordre de mission non trouvé : " + e.getMessage());
        } catch (JRException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération du rapport JasperReports : " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur d'entrée/sortie lors de la lecture du template : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur inattendue lors de la génération du PDF : " + e.getMessage());
        }
    }

    /**
     * Génère un PDF pour chaque employé du déplacement
     * Si plusieurs employés : retourne un ZIP avec un PDF par employé
     * Si un seul employé : retourne un seul PDF
     */
    @GetMapping("/Deplacement/ImprimerTous/{id}")
    @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<?> generateDemandeForAllEmployees(@PathVariable("id") Long id) {
        try {
            // Vérifier que l'ID existe
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("ID de déplacement invalide : " + id);
            }

            ResponseEntity<byte[]> OmEtat = deplacementImpService.generateOmForAllEmployees(id);
            return OmEtat;

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Fichier template JasperReports non trouvé : " + e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Déplacement non trouvé avec l'ID : " + id);
        } catch (OrdreMissionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ordre de mission non trouvé : " + e.getMessage());
        } catch (JRException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération du rapport : " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur d'entrée/sortie : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur inattendue : " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("/upload");
        String message = "";
        try {
            storageService.save(file);
            message = "Le fichier a été téléchargé avec succès: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Impossible de télécharger le fichier: " + file.getOriginalFilename() + ". Erreur: "
                    + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/savePjDeplacementById/{id}")
    ResponseEntity<ResponseMessage> savePjSuiviCaisse(@PathVariable("id") long id,
            @RequestParam("file") MultipartFile file) {

        String message = "";
        try {
            storageService.savePjDeplacementById(id, file);
            message = "Le fichier a été téléchargé avec succès: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Impossible de télécharger le fichier: " + file.getOriginalFilename() + ". Erreur: "
                    + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

}