package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.*;
import ma.gap.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des projets et des articles associés.
 */
@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600) // Permet l'accès CORS pour tous les domaines
@RestController
@RequestMapping("/api/project") // Mapping de la route de base pour ce contrôleur
public class ProjetController {

    private ProjetService projetService;
    private UserImpService userImpService;
    private ProjetRepository projetRepository;

    /**
     * Endpoint pour récupérer tous les projets.
     *
     * @return Liste des projets
     */
    @GetMapping(value = "/getAll")
    public List<Projet> getAllProject() {
        return projetService.allProjet();
    }

    @GetMapping(value = "/searchPaginated")
    public Page<Projet> searchProjets(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return projetService.searchProjets(keyword, pageable);
    }

    @GetMapping(value = "/getAllByStatus/{status}")
    public List<Projet> getAllProjectByStatus(@PathVariable("status") int status) {
        return projetService.getAllProjetsByStatus(status);
    }

    /**
     * Endpoint pour ajouter un nouveau projet.
     *
     * @param projet Projet à ajouter
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @PostMapping(value = "/addProject")
    public ResponseEntity<?> saveProject(@RequestBody Projet projet) {
        try {
            Projet savedProjet = projetService.saveProjet(projet);
            return ResponseEntity.status(HttpStatus.CREATED).body("Projet enregistré avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    /**
     * Endpoint pour éditer un projet existant.
     *
     * @param id     Identifiant du projet à éditer
     * @param projet Objet projet avec les nouvelles données
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @PutMapping(value = "/updateProject/{id}")
    public ResponseEntity<String> editProject(@PathVariable("id") long id, @RequestBody Projet projet) {
        try {
            Optional<Projet> optionalProjet = projetService.findById(id);
            if (!optionalProjet.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Projet non trouvé. Édition impossible.");
            }
            projetService.updateProjet(projet, id);
            return ResponseEntity.ok("Projet édité avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    /**
     * Endpoint pour supprimer un projet.
     *
     * @param id Identifiant du projet à supprimer
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @DeleteMapping(value = "/deleteProject/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable("id") long id) {
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.ok("Projet supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    /**
     * Endpoint pour récupérer les affaires (projets) en fonction d'un atelier
     * donné.
     *
     * @param idUser Identifiant de l'utilisateur connecté'
     * @return Liste des projets associés à l'atelier d'user
     */
    @GetMapping("/by-atelier/{idUser}")
    public ResponseEntity<?> getAffairesByAtelier(@PathVariable Long idUser) {
        /*
         * List<Projet> affaires = projetService.getAffairesByAtelier(atelierId);
         * return ResponseEntity.ok(affaires);
         */
        try {
            User user = userImpService.finduserById(idUser);
            boolean isAdminOrLogistique = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("admin") || role.getName().equals("logistique")
                            || role.getName().equals("consulteur"));
            if (isAdminOrLogistique) {
                return ResponseEntity.ok(projetRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
            } else {
                long idAtelier = user.getAteliers().get(0).getId();
                return ResponseEntity.ok(projetService.getAffairesByAtelier(idAtelier));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des livraisons.");
        }
    }

    @GetMapping(value = "/Projets/Search")
    public List<Projet> getProjectFiltred(@RequestParam("code") String code, @RequestParam("affaire") String affaire,
            @RequestParam("atelier") String atelier, @RequestParam("article") String article) {
        return projetService.ProjetFiltred(code, affaire, article, atelier);
    }

    @GetMapping(value = "/findAffairesByAtelierAndQteArticle_Sup_QteOF")
    public ResponseEntity<?> findAffairesByAtelierAndQteArticle_Sup_QteOF(@RequestParam("idatelier") long idatelier) {
        try {

            List<Projet> projets = projetService.findAffairesByAtelierAndQteArticle_Sup_QteENPROD(idatelier);

            return ResponseEntity.ok(projets);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des projets.");
        }
    }
}
