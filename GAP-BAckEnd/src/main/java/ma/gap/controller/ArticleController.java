package ma.gap.controller;

import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;

import ma.gap.service.ArticleAchService;
import ma.gap.service.ArticleService;
import ma.gap.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/article")
@AllArgsConstructor
public class ArticleController {
    private ArticleService articleService;
    private UserService userService;

    @GetMapping(value = "/getAll/{idUser}")
    public ResponseEntity<?> findAllArticles(@PathVariable("idUser") long idUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Article> articles;
            User user = userService.finduserById(idUser);
            boolean isAdminOrLogistique = userService.isRoleAutorize(user.getId());
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

            if (isAdminOrLogistique) {
                articles = articleService.findAll(pageable);
            } else {
                articles = articleService.allArticleByAtelier(user.getAteliers().get(0).getId(), pageable);
            }
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des articles.");
        }
    }

    /**
     * Endpoint pour récupérer tous les articles associés à un projet.
     *
     * @param idUser Identifiant de l'utilisateur
     * @param projet Identifiant du projet
     * @return Liste des articles associés au projet
     */
    @GetMapping(value = "/Article/Tous/{idUser}/{id}")
    public List<Article> getAllArticleByProject(@PathVariable("idUser") long idUser, @PathVariable("id") long projet) {
        return articleService.allArticleByProject(idUser, projet);
    }

    /**
     * Endpoint pour ajouter un nouvel article à un projet.
     *
     * @param article Objet Article à ajouter
     * @return Redirection vers la page des articles du projet
     */
    @PostMapping(value = "/add")
    public ResponseEntity<?> save(@RequestBody Article article) {
        try {
            long idArticle = article.getId();
            articleService.saveArticle(article, idArticle);

            return ResponseEntity.status(HttpStatus.CREATED).body("Article enregistré avec succès."); // Retourner l'ID
                                                                                                      // dans la réponse
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @PutMapping(value = "/update/{id}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<String> edit(@PathVariable("id") long id, @RequestBody Article article) {
        try {
            Optional<Article> optionalArticle = articleService.findById(id);

            if (!optionalArticle.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article non trouvée. Édition impossible.");
            }

            articleService.editArticle(article, id);
            return ResponseEntity.ok("Article édité avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<String> deleteLivraison(@PathVariable("id") long id) {
        try {

            articleService.deleteArticle(id);
            return ResponseEntity.ok("Article supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping(value = "/articlesByProjet")
    public ResponseEntity<?> allArticleByProject(@RequestParam("idUser") long idUser,
            @RequestParam("idprojet") long idprojet) {
        try {

            List<Article> articles = articleService.allArticleByProject(idUser, idprojet);
            articles.sort(Comparator.comparing(Article::getId).reversed());
            return ResponseEntity.ok(articles);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des articles.");
        }
    }

    @GetMapping("/by-atelier/{idUser}")
    public ResponseEntity<?> getArticlesByAtelier(@PathVariable Long idUser) {

        try {
            User user = userService.finduserById(idUser);
            boolean isAdminOrLogistique = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("admin") || role.getName().equals("logistique")
                            || role.getName().equals("consulteur"));
            if (isAdminOrLogistique) {
                return ResponseEntity.ok(articleService.findAll(Sort.by(Sort.Direction.DESC, "id")));
            } else {
                long idAtelier = user.getAteliers().get(0).getId();
                return ResponseEntity.ok(articleService.allArticleByAtelier(idAtelier));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des livraisons.");
        }
    }

    @GetMapping("/search")
    public org.springframework.data.domain.Page<Article> searchArticles(@RequestParam(value = "idUser") long idUser,
            @RequestParam("numPrix") String numPrix,
            @RequestParam("designation") String designation,
            @RequestParam("idprojet") long idProjet,
            @RequestParam("idatelier") long idAtelier,
            @RequestParam("idarticle") long idArticle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ParseException {
        Pageable pageable = PageRequest.of(page, size);
        return articleService.searchArticlePaginated(idUser, numPrix, designation, idProjet, idAtelier, idArticle,
                pageable);
    }

    @GetMapping(value = "/findArticles_QteSup_QteOF")
    public ResponseEntity<?> findArticles_QteSup_QteOF(@RequestParam("idprojet") long idprojet,
            @RequestParam("idatelier") long idatelier) {
        try {
            List<Article> articles = articleService.findArticles_QteSup_QteOF(idprojet, idatelier);
            articles.sort(Comparator.comparing(Article::getId).reversed());
            return ResponseEntity.ok(articles);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des articles.");
        }
    }
}
